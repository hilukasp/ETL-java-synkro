package sptech.school;

import io.github.cdimascio.dotenv.Dotenv;

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
        List<String[]> dadosMainframe = ConnexaoAws.lerArquivoCsvDoRaw("dados-mainframe.csv");
        List<String[]> dadosProcesso = ConnexaoAws.lerArquivoCsvDoRaw("processos.csv");

        importarArquivoCSVMaquinaMemoria(dadosMainframe, listaLidoMainframe);
        importarArquivoCSVProcessoMemoria(dadosProcesso, listaLidoProcesso);

        // 🔹 Gera CSV tratado e envia pro bucket TRUSTED
        String csvTratado = gerarCsvTrusted(listaLidoMainframe, listaLidoProcesso);
        ConnexaoAws.enviarCsvTrusted("trusted.csv", csvTratado);

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

                // Contadores
                int countCpu = 0, countRam = 0, countDisco = 0, countSwap = 0;
                int countOc = 0, countWait = 0, countThru = 0, countIops = 0;
                int countRead = 0, countWrite = 0, countLat = 0;

                List<List<Object>> componentes = ConnexaoBd.buscarMetricas(conn, macAdress);

                for (List<Object> c : componentes) {
                    int fkcomp = (Integer) c.get(0);
                    Double min = (Double) c.get(1);
                    Double max = (Double) c.get(2);
                    String nomecomponente = (String) c.get(3);
                    Integer qtdIncidencias = 5;
                    double valor = 0;

                    boolean gerarAlerta = false;

                    switch (fkcomp) {
                        case 1:
                            valor = usoCpu;
                            if (valor < min || valor > max) {
                                countCpu++;
                                if (countCpu >= qtdIncidencias) {
                                    gerarAlerta = true;
                                    countCpu = 0;
                                }
                                break;
                            }
                        case 2:
                            valor = usoRam;
                            if (valor < min || valor > max) countRam++;
                        {
                            if (countRam >= qtdIncidencias) {
                                gerarAlerta = true;
                                countRam = 0;
                            }
                            break;
                        }
                        case 3:
                            valor = usoDisco;
                            if (valor < min || valor > max) {
                                countDisco++;
                                if (countDisco >= qtdIncidencias) {
                                    gerarAlerta = true;
                                    countDisco = 0;
                                }
                                break;
                            }
                        case 4:
                            valor = swapRate;
                            if (valor < min || valor > max) {
                                countSwap++;
                                if (countSwap >= qtdIncidencias) {
                                    gerarAlerta = true;
                                    countSwap = 0;
                                }
                                break;
                            }
                        case 5:
                            valor = cpuOciosa;
                            if (valor < min || valor > max) {
                                countOc++;
                                if (countOc >= qtdIncidencias) {
                                    gerarAlerta = true;
                                    countOc = 0;
                                }
                                break;
                            }
                        case 6:
                            valor = cpuIoWait;
                            if (valor < min || valor > max) {
                                countWait++;
                                if (countWait >= qtdIncidencias) {
                                    gerarAlerta = true;
                                    countWait = 0;
                                }
                                break;
                            }
                        case 7:
                            valor = throughput;
                            if (valor < min || valor > max) {
                                countThru++;
                                if (countThru >= qtdIncidencias) {
                                    gerarAlerta = true;
                                    countThru = 0;
                                }
                                break;
                            }
                        case 8:
                            valor = discIops;
                            if (valor < min || valor > max) {
                                countIops++;
                                if (countIops >= qtdIncidencias) {
                                    gerarAlerta = true;
                                    countIops = 0;
                                }
                                break;
                            }
                        case 9:
                            valor = read;
                            if (valor < min || valor > max) {
                                countRead++;
                                if (countRead >= qtdIncidencias) {
                                    gerarAlerta = true;
                                    countRead = 0;
                                }
                                break;
                            }
                        case 10:
                            valor = write;
                            if (valor < min || valor > max) {
                                countWrite++;
                                if (countWrite >= qtdIncidencias) {
                                    gerarAlerta = true;
                                    countWrite = 0;
                                }
                                break;
                            }
                        case 11:
                            valor = latenciaDisc;
                            if (valor < min || valor > max) {
                                countLat++;
                                if (countLat >= qtdIncidencias) {
                                    gerarAlerta = true;
                                    countLat = 0;
                                }
                                break;
                            }
                    }

                    if (gerarAlerta) {
                        System.out.println(" Alerta: Componente " + fkcomp + " fora dos limites");
                        ConnexaoBd.inserirAlerta(conn, data, fkcomp, valor, macAdress, nomecomponente);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao conectar no banco: " + e.getMessage());
        }
    }

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
                    System.out.println("Erro import");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar dados de Processo.");
        }
    }

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
