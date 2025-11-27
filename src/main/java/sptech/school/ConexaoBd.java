package sptech.school;

import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static sptech.school.IntegracaoJira.abrirChamado;

public class ConexaoBd {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conexão estabelecida com sucesso! \n");
        } catch (SQLException e) {
            System.err.println("Erro na conexão: " + e.getMessage());
        }
    }

    // Insere alerta e abre chamado no Jira
    public static void inserirAlerta(@NotNull Connection conn,
                                     String dtHora, Integer fkComponente, Object valorColetado,
                                     String macAdress, String nomeComponente,String metrica) {
        String sql = """
        INSERT INTO alerta (dt_hora, valor_coletado, fkMetrica, fkStatus)
        VALUES (
            ?, ?, 
            (SELECT m.id
             FROM metrica m
             JOIN mainframe mf ON m.fkMainframe = mf.id
             WHERE m.fkComponente = ? AND mf.macAdress = ?
             LIMIT 1),
            1
        )
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dtHora);
            stmt.setObject(2, valorColetado);
            stmt.setInt(3, fkComponente);
            stmt.setString(4, macAdress);

            stmt.executeUpdate();

            String descricao = "Valor " + metrica + " fora do limite: " + valorColetado +
                    " || componente: " + nomeComponente +
                    " || macAdress: " + macAdress +
                    " || hora: " + dtHora;

            System.out.println("Alerta inserido para " + nomeComponente);
            abrirChamado("Alerta no " + nomeComponente, descricao);

        } catch (SQLException e) {
            System.err.println("Erro ao inserir alerta: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Busca métricas configuradas para um mainframe
    public static List<List<Object>> buscarMetricas(Connection conn, String macAdress) throws SQLException {
        String sql = """
        SELECT fkComponente, min, max, c.nome
        FROM metrica m
        JOIN componente c ON m.fkComponente = c.id
        JOIN mainframe mf ON m.fkMainframe = mf.id
        WHERE mf.macAdress = ?
        """;

        List<List<Object>> lista = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, macAdress);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(List.of(
                        rs.getInt("fkComponente"),
                        rs.getDouble("min"),
                        rs.getDouble("max"),
                        rs.getString("nome")
                ));
            }
        }
        return lista;
    }

    //lista empresa
    public static List<String> listaEmpresas(Connection conn) throws SQLException {
        String sql = "SELECT id FROM empresa;";

        List<String> lista = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("id"));
            }
        }catch (SQLException e) {
            System.err.println("erro em listar empresa: " + e.getMessage());
        }

        return lista;
    }
}
