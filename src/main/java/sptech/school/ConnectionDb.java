package sptech.school;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConnectionDb {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        // Conectar ao banco
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conexão estabelecida com sucesso!");
            System.out.println( buscarMetricas(conn, "269058769682378"));
        } catch (SQLException e) {
            System.err.println("Erro na conexão: " + e.getMessage());
        }


    }

    public static void inserirAlerta(@org.jetbrains.annotations.NotNull Connection conn,
                                     String dtHora, Integer fkComponente, Object valorColetado, String macAdress) {

        String sql = """
                INSERT INTO alerta (dt_hora, fkComponente, valor_coletado, fkMainframe)
                VALUES (?, ?, ?, (SELECT id FROM mainframe WHERE macAdress = ?))
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dtHora);
            stmt.setInt(2, fkComponente);
            stmt.setObject(3, valorColetado);
            stmt.setString(4, macAdress);

            stmt.executeUpdate();
            System.out.println(" Alerta inserido");

        } catch (SQLException e) {
            System.err.println(" Erro ao inserir alerta: " + e.getMessage());
        }
    }


    public static List<List<Object>> buscarMetricas(@org.jetbrains.annotations.NotNull Connection conn, String macAdress) {

        List<List<Object>> allComponentes = new ArrayList<>();


        String sql = """
                SELECT fkcomponente,min,max FROM componente_mainframe cm
                JOIN componente cp ON cm.fkcomponente = cp.id
                JOIN metrica mt ON cp.fkMetrica = mt.id
                WHERE fkMainframe = (SELECT id FROM mainframe WHERE macAdress = ?) and\s
                cp.captura = 1;
                """;


        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, macAdress);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Integer componente = rs.getInt("fkcomponente");
                    Double min = rs.getDouble("min");
                    Double max = rs.getDouble("max");

                    List<Object> comp_met = new ArrayList<>();
                    comp_met.add(componente);
                    comp_met.add(min);
                    comp_met.add(max);

                    allComponentes.add(comp_met);

                }
                return allComponentes;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar alertas: " + e.getMessage());
            return null;
        }
    }

}
