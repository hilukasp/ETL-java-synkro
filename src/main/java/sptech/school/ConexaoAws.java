package sptech.school;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.util.*;

public class ConexaoAws {

    private static final Region REGION = Region.US_EAST_1;
    private static final S3Client s3 = S3Client.builder()
            .region(REGION)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    private static final List<String> BUCKETS_RAW = new ArrayList<>();
    private static final List<String> BUCKETS_TRUSTED = new ArrayList<>();
    private static final List<String> BUCKETS_CLIENT = new ArrayList<>();

    //lista os diretórios do bucket
    public static List<String> listarDiretorios(String idEmpresa) {
        List<String> diretorios = new ArrayList<>();
        String bucket = pegarBucket("raw");

        try {
            if (!idEmpresa.endsWith("/")) {
                idEmpresa += "/";
            }

            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(idEmpresa)
                    .delimiter("/")
                    .build();

            ListObjectsV2Response res = s3.listObjectsV2(listReq);

            res.commonPrefixes().forEach(cp -> diretorios.add(cp.prefix()));

            System.out.println("Diretórios encontrados dentro de " + idEmpresa + ": " + diretorios);

        } catch (Exception e) {
            System.err.println("Erro ao listar diretórios: " + e.getMessage());
        }

        return diretorios;
    }



    //Lê um CSV do bucket RAW e devolve como lista de linhas
    public static List<String[]> lerArquivoCsvDoRaw(String nomeArquivo) {

        List<String[]> linhas = new ArrayList<>();
        String bucketRaw = pegarBucket("raw");

        try {
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(bucketRaw)
                    .key(nomeArquivo)
                    .build();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(s3.getObject(getReq)))) {

                String linha;
                while ((linha = reader.readLine()) != null) {
                    linhas.add(linha.split(";"));
                }
            }

            System.out.println("Arquivo lido do RAW: " + nomeArquivo);

        } catch (Exception e) {
            System.err.println("Erro ao ler arquivo do RAW: " + e.getMessage());
        }

        return linhas;
    }

    //Envia CSV tratado para o bucket TRUSTED
    public static void enviarCsvTrusted(String nomeArquivo, String conteudoCsv) {
        String bucketTrusted = pegarBucket("trusted");

        try {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucketTrusted)
                    .key(nomeArquivo)
                    .build();

            s3.putObject(putReq, RequestBody.fromString(conteudoCsv));
            System.out.println("✅ CSV tratado enviado para TRUSTED: " + nomeArquivo);

        } catch (Exception e) {
            System.err.println("Erro ao enviar CSV para TRUSTED: " + e.getMessage());
        }
    }

    //Acha o bucket certo pelo nome (raw/trusted/client)
    private static String pegarBucket(String tipo) {
        ListBucketsResponse response = s3.listBuckets();
        for (Bucket b : response.buckets()) {
            if (b.name().toLowerCase().contains(tipo)) {
                return b.name();
            }
        }
        throw new RuntimeException("Bucket do tipo '" + tipo + "' não encontrado!");
    }

    //Lista todos os buckets da conta
    public static List<String> pegarBucketsS3() {
        List<String> buckets = new ArrayList<>();
        try {
            ListBucketsResponse response = s3.listBuckets();
            for (Bucket b : response.buckets()) {
                buckets.add(b.name());
            }
        } catch (S3Exception e) {
            System.err.println("Erro ao listar buckets: " + e.awsErrorDetails().errorMessage());
        }
        return buckets;
    }

    public static void consolidarArquivosTrusted(String idEmpresa, String macAdress, List<String> diretoriosData) {
        String bucketTrusted = pegarBucket("trusted");
        String keyGeral = String.format("%s/%s/geral/trusted.csv", idEmpresa, macAdress);

        StringBuilder conteudoConsolidado = new StringBuilder();
        boolean primeiroArquivo = true;

        String HEADER = "macAdress;timestamp;identificao-mainframe;uso_cpu_total_%;uso_ram_total_%;uso_disco_total_%;disco_throughput_mbs;disco_iops_total;disco_read_count;disco_write_count;disco_latencia_ms;nome1;cpu_%1;mem_%1;nome2;cpu_%2;mem_%2;nome3;cpu_%3;mem_%3;nome4;cpu_%4;mem_%4;nome5;cpu_%5;mem_%5;nome6;cpu_%6;mem_%6;nome7;cpu_%7;mem_%7;nome8;cpu_%8;mem_%8;nome9;cpu_%9;mem_%9;\n";

        // Anexa o cabeçalho ao conteúdo consolidado ANTES de começar a ler
        conteudoConsolidado.append(HEADER);


        for (String diretorioData : diretoriosData) {
            String keyDiario = diretorioData + "trusted.csv"; // Ex: 1/269166201749049/02122025/trusted.csv

            try {
                GetObjectRequest getReq = GetObjectRequest.builder()
                        .bucket(bucketTrusted)
                        .key(keyDiario)
                        .build();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(s3.getObject(getReq)))) {

                    String linha;

                    // Pula o cabeçalho do arquivo diário
                    reader.readLine();

                    // Anexa o restante do conteúdo
                    while ((linha = reader.readLine()) != null) {
                        conteudoConsolidado.append(linha).append("\n");
                    }
                }

            } catch (NoSuchKeyException e) {
                System.out.println("Aviso: Arquivo diário não encontrado em " + keyDiario);
            } catch (Exception e) {
                System.err.println("Erro ao ler arquivo diário para consolidação: " + e.getMessage());
            }
        }

        // Se houver conteúdo além do cabeçalho para consolidar
        if (conteudoConsolidado.length() > HEADER.length()) {

            // Faz o upload do novo conteúdo consolidado
            try {
                PutObjectRequest putReq = PutObjectRequest.builder()
                        .bucket(bucketTrusted)
                        .key(keyGeral)
                        .build();

                s3.putObject(putReq, RequestBody.fromString(conteudoConsolidado.toString()));
                System.out.println("✅ CSV CONSOLIDADO (GERAL) enviado para: " + keyGeral);
            } catch (Exception e) {
                System.err.println("Erro ao enviar CSV consolidado: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Consolidação Geral ignorada: Nenhum dado diário encontrado para " + idEmpresa + "/" + macAdress);
        }
    }

    //Testa conexão e categoriza buckets
    public static void main(String[] args) {
        try {
            System.out.println("Conectando à AWS S3...");
            List<String> buckets = pegarBucketsS3();

            for (String b : buckets) {
                String nome = b.toLowerCase();
                if (nome.contains("raw")) BUCKETS_RAW.add(b);
                else if (nome.contains("trusted")) BUCKETS_TRUSTED.add(b);
                else if (nome.contains("client")) BUCKETS_CLIENT.add(b);
            }

            System.out.println("\nBuckets RAW: " + BUCKETS_RAW);
            System.out.println("Buckets TRUSTED: " + BUCKETS_TRUSTED);
            System.out.println("Buckets CLIENT: " + BUCKETS_CLIENT);

        } catch (Exception e) {
            System.err.println("Erro ao conectar ou listar buckets: " + e.getMessage());
        }
    }
}
