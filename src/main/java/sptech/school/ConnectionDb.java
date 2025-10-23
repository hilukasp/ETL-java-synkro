package sptech.school;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class ConnectionDb {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        // Conectar ao banco
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conexão estabelecida com sucesso!");
            buscarMetricas(conn, "269058769682378");
        } catch (SQLException e) {
            System.err.println("Erro na conexão: " + e.getMessage());
        }


    }

    public static void inserirAlerta(@org.jetbrains.annotations.NotNull Connection conn,
                                     String dtHora, String descricao, Double valorColetado, String macAdress
    ) {

        String sql = """
                INSERT INTO alerta (dt_hora, descricao, valor_coletado, fkMainframe)
                VALUES (?, ?, ?, (SELECT id FROM mainframe WHERE macAdress = ?))
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dtHora);
            stmt.setString(2, descricao);
            stmt.setDouble(3, valorColetado);
            stmt.setString(4, macAdress);

            stmt.executeUpdate();
            System.out.println(" Alerta inserido");

        } catch (SQLException e) {
            System.err.println(" Erro ao inserir alerta: " + e.getMessage());
        }
    }


    public static void buscarMetricas(@org.jetbrains.annotations.NotNull Connection conn, String macAdress) {
        String sql = """
                SELECT mt.min, mt.max
                FROM componente_mainframe cm
                JOIN componente cp ON cm.fkcomponente = cp.id
                JOIN metrica mt ON cp.fkMetrica = mt.id
                WHERE fkMainframe = (SELECT id FROM mainframe WHERE macAdress = ?)
                """;


          try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, macAdress);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Double min = rs.getDouble("min");
                    Double max = rs.getDouble("max");
                    System.out.printf(" min %.2f | max %.2f", min, max);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar alertas: " + e.getMessage());
        }
    }

}
