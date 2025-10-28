package sptech.school;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.util.*;

public class ConnectionAws {

    private static final Region REGION = Region.US_EAST_1;
    private static S3Client s3;

    static {
        s3 = S3Client.builder()
                .region(REGION)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    // 🔹 Lê um CSV do bucket RAW e devolve como lista de linhas
    public static List<String[]> lerArquivoCsvDoRaw(String nomeArquivo) {
        String bucketRaw = pegarBucket("raw");
        List<String[]> linhas = new ArrayList<>();

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
            System.out.println("Erro ao ler arquivo do RAW: " + e.getMessage());
        }

        return linhas;
    }

    // 🔹 Envia CSV tratado para o bucket TRUSTED
    public static void enviarCsvTrusted(String nomeArquivo, String conteudoCsv) {
        String bucketTrusted = pegarBucket("trusted");

        try {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucketTrusted)
                    .key(nomeArquivo)
                    .build();

            s3.putObject(putReq, RequestBody.fromString(conteudoCsv));
            System.out.println("CSV tratado enviado para TRUSTED: " + nomeArquivo);

        } catch (Exception e) {
            System.out.println("Erro ao enviar CSV para TRUSTED: " + e.getMessage());
        }
    }

    // 🔹 Acha o bucket certo pelo nome (raw/trusted/client)
    private static String pegarBucket(String tipo) {
        ListBucketsResponse response = s3.listBuckets();
        for (Bucket b : response.buckets()) {
            if (b.name().toLowerCase().contains(tipo)) {
                return b.name();
            }
        }
        throw new RuntimeException("Bucket do tipo " + tipo + " não encontrado!");
    }
}
