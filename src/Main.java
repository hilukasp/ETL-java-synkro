import java.io.*;
import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        List<Mainframe> listaLido=new ArrayList<>();
        importarArquivoCSV("dados-mainframe",listaLido);
        //listarObjeto(listaLido);
    }

    public static void importarArquivoCSV(String nomeArq,List<Mainframe> listaLido){
        Reader arq = null; //arq eh o objeto que corresponde o arquivo
        BufferedReader entrada =null; //entrada eh o objeto usado para ler do arquivo
        nomeArq+=".csv";


        //bloco trycatch para abrir o arquivo
        try {
            arq=new InputStreamReader(new FileInputStream(nomeArq),"UTF-8");
            entrada=new BufferedReader(arq);
        }catch (IOException erro){
            System.out.println("Erro na abertura do arquivo");
            System.exit(1);
        }

        try {
            String[] registro; //registro é um vetor que armazenará toda as linhas do arquivo
            String linha=entrada.readLine(); //le somenta uma linha inteira
            registro=linha.split(";");
            System.out.printf("%1s %40s %19s %20s %14s %20s %20s %20s %20s %20s %20s %20s %20s\n",registro[0],registro[1],registro[2],registro[3],registro[4],registro[5],registro[6],registro[7],registro[8],registro[9],registro[10],registro[11],registro[12]);

            //ler a segunda linha do arquivo
            linha = entrada.readLine();
            while (linha!=null){
                registro = linha.split(";");

                Mainframe mainframe = new Mainframe();
                mainframe.setTimestamp(registro[0]);
                mainframe.setIdentificaoMainframe(registro[1]);
                mainframe.setUsoCpuTotal(Integer.valueOf(registro[2]));
                mainframe.setUsoRamTotal(Integer.valueOf(registro[3]));
                mainframe.setSwapRateMbs(Integer.valueOf(registro[4]));
                mainframe.setTempoCpuOciosa(Integer.valueOf(registro[5]));
                mainframe.setCpuIoWait(Integer.valueOf(registro[6]));
                mainframe.setUsoDiscoTotal(Integer.valueOf(registro[7]));
                mainframe.setDiscoIopsTotal(Integer.valueOf(registro[8]));
                mainframe.setDiscoThroughputMbs(Integer.valueOf(registro[9]));
                mainframe.setDiscoReadCount(Integer.valueOf(registro[10]));
                mainframe.setDiscoWriteCount(Integer.valueOf(registro[11]));
                mainframe.setDiscoLatenciaMs(Double.valueOf(registro[12].replace(",", ".")));

                System.out.printf("%1s %16s %20s %20s %20s %20s %20s %20s %20s %20s %20s %20s %20s\n",registro[0],registro[1],registro[2],registro[3],registro[4],registro[5],registro[6],registro[7],registro[8],registro[9],registro[10],registro[11],registro[12]);

                listaLido.add(mainframe);
                linha =entrada.readLine();
            }
        }catch (IOException erro){
            System.out.println("erro ao ler arquivo");
            erro.printStackTrace();
        }
        finally {
            try {
                entrada.close();
                arq.close();
            }catch (IOException erro){
                System.out.println("Erro ao fechar o arquivo");
            }
        }

    }
    public static void listarObjeto(List<Mainframe> listaLido){
        System.out.println("\nLista lida do arquivo");
        for (Mainframe mainframe:listaLido){
            System.out.println(mainframe);
        }
    }
}