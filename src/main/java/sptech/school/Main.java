package sptech.school;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        List<Mainframe> listaLidoMainframe = new ArrayList<>();
        List<Processo> listaLidoProcesso = new ArrayList<>();

        // 🔹 Baixa e trata os CSVs do bucket RAW direto da AWS
        List<String[]> dadosMainframe = ConnectionAws.lerArquivoCsvDoRaw("dados-mainframe.csv");
        List<String[]> dadosProcesso = ConnectionAws.lerArquivoCsvDoRaw("processos.csv");

        importarArquivoCSVMaquinaMemoria(dadosMainframe, listaLidoMainframe);
        importarArquivoCSVProcessoMemoria(dadosProcesso, listaLidoProcesso);

        // 🔹 Gera CSV tratado e envia pro bucket TRUSTED
        String csvTratado = gerarCsvTrusted(listaLidoMainframe, listaLidoProcesso);
        ConnectionAws.enviarCsvTrusted("trusted.csv", csvTratado);

        // 🔹 Valida alertas no Synkro
        validarAlerta(listaLidoMainframe, listaLidoProcesso);
    }

    public static void validarAlerta(List<Mainframe> listamainframe, List<Processo> listaprocesso) {
        try (Connection conn = DriverManager.getConnection(
                Dotenv.load().get("DB_URL"),
                Dotenv.load().get("DB_USER"),
                Dotenv.load().get("DB_PASSWORD"))) {

            for (Mainframe mainframe : listamainframe) {
                String data = mainframe.getTimestamp();
                String macAdress = mainframe.getMacAdress();

                double usoDisco = mainframe.getUsoDiscoTotal();
                double usoRam = mainframe.getUsoRamTotal();
                double usoCpu = mainframe.getUsoCpuTotal();
                double cpuOciosa = mainframe.getTempoCpuOciosa();
                double cpuIoWait = mainframe.getCpuIoWait();
                double swapRate = mainframe.getSwapRateMbs();
                double throughput = mainframe.getDiscoThroughputMbs();
                double discIops = mainframe.getDiscoIopsTotal();
                double read = mainframe.getDiscoReadCount().doubleValue();
                double write = mainframe.getDiscoWriteCount().doubleValue();
                double latenciaDisc = mainframe.getDiscoLatenciaMs();

                List<List<Object>> componentes = ConnectionDb.buscarMetricas(conn, macAdress);

                for (List<Object> c : componentes) {
                    int fkcomp = (Integer) c.get(0);
                    double min = (Double) c.get(1);
                    double max = (Double) c.get(2);
                    String nomecomponente = (String) c.get(3);

                    double valor = switch (fkcomp) {
                        case 1 -> usoCpu;
                        case 2 -> usoRam;
                        case 3 -> usoDisco;
                        case 4 -> swapRate;
                        case 5 -> cpuOciosa;
                        case 6 -> cpuIoWait;
                        case 7 -> throughput;
                        case 8 -> discIops;
                        case 9 -> read;
                        case 10 -> write;
                        case 11 -> latenciaDisc;
                        default -> 0;
                    };

                    if (valor < min || valor > max) {
                        System.out.println(" Alerta Componente " + fkcomp + " fora dos limites");
                        ConnectionDb.inserirAlerta(conn, data, fkcomp, valor, macAdress, nomecomponente);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao conectar no banco: " + e.getMessage());
        }
    }

    // 🔹 Importa dados de Mainframe a partir da lista em memória
    public static void importarArquivoCSVMaquinaMemoria(List<String[]> dados, List<Mainframe> listaLido) {
        try {
            SimpleDateFormat dtEntrada = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            SimpleDateFormat dtSaida = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int i = 1; i < dados.size(); i++) {
                String[] registro = dados.get(i);
                Mainframe mainframe = new Mainframe();

                try {
                    mainframe.setMacAdress(registro[0]);
                    Date date = dtEntrada.parse(registro[1]);
                    mainframe.setTimestamp(dtSaida.format(date));
                    mainframe.setIdentificaoMainframe(registro[2]);
                    mainframe.setUsoCpuTotal(Double.valueOf(registro[3].replace(",", ".")));
                    mainframe.setUsoRamTotal(Double.valueOf(registro[4].replace(",", ".")));
                    mainframe.setSwapRateMbs(Double.valueOf(registro[5].replace(",", ".")));
                    mainframe.setTempoCpuOciosa(Double.valueOf(registro[6].replace(",", ".")));
                    mainframe.setCpuIoWait(Double.valueOf(registro[7].replace(",", ".")));
                    mainframe.setUsoDiscoTotal(Double.valueOf(registro[8].replace(",", ".")));
                    mainframe.setDiscoThroughputMbs(Double.valueOf(registro[9].replace(",", ".")));
                    mainframe.setDiscoIopsTotal(Double.valueOf(registro[10].replace(",", ".")));
                    mainframe.setDiscoReadCount(Integer.valueOf(registro[11]));
                    mainframe.setDiscoWriteCount(Integer.valueOf(registro[12]));
                    mainframe.setDiscoLatenciaMs(Double.valueOf(registro[13].replace(",", ".")));

                    listaLido.add(mainframe);
                } catch (NumberFormatException | ParseException erro) {
                    System.out.println("Linha ignorada por erro de formatação.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar dados do Mainframe.");
        }
    }

    // 🔹 Importa dados de Processo a partir da lista em memória
    public static void importarArquivoCSVProcessoMemoria(List<String[]> dados, List<Processo> listaLidoProcesso) {
        try {
            for (int i = 1; i < dados.size(); i++) {
                String[] registro = dados.get(i);
                Processo processo = new Processo();

                try {
                    processo.setTimestamp(registro[0]);
                    processo.setMacAdress(registro[1]);
                    processo.setIdentificacaoMainframe(registro[2]);
                    processo.setNome1(registro[6]);
                    processo.setCpu1(Double.parseDouble(registro[7].replace(",", ".")));
                    processo.setMem1(Double.parseDouble(registro[8].replace(",", ".")));
                    listaLidoProcesso.add(processo);
                } catch (NumberFormatException erro) {
                    System.out.println("Linha ignorada no processo por erro de número.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar dados de Processo.");
        }
    }

    // 🔹 Gera CSV tratado em memória
    public static String gerarCsvTrusted(List<Mainframe> listaMainframe, List<Processo> listaProcesso) {
        StringBuilder sb = new StringBuilder();
        sb.append("macAdress;timestamp;identificao-mainframe;uso_cpu_total_%;uso_ram_total_%;swap_rate_mbs;tempo_cpu_ociosa;cpu_io_wait;uso_disco_total_%;disco_throughput_mbs;disco_iops_total;disco_read_count;disco_write_count;disco_latencia_ms\n");

        for (Mainframe m : listaMainframe) {
            sb.append(String.format("%s;%s;%s;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f;%d;%d;%.2f\n",
                    m.getMacAdress(), m.getTimestamp(), m.getIdentificaoMainframe(),
                    m.getUsoCpuTotal(), m.getUsoRamTotal(), m.getSwapRateMbs(),
                    m.getTempoCpuOciosa(), m.getCpuIoWait(), m.getUsoDiscoTotal(),
                    m.getDiscoThroughputMbs(), m.getDiscoIopsTotal(),
                    m.getDiscoReadCount(), m.getDiscoWriteCount(), m.getDiscoLatenciaMs()));
        }

        return sb.toString();
    }
}
