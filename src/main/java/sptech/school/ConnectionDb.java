package sptech.school;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static sptech.school.JiraIntegration.abrirChamado;

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
                                     String dtHora, Integer fkComponente, Object valorColetado, String macAdress,String nomecomponente) {

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
            abrirChamado("ERRO no "+nomecomponente,"alerta de: "+valorColetado);

        } catch (SQLException e) {
            System.err.println(" Erro ao inserir alerta: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    public static List<List<Object>> buscarMetricas(@org.jetbrains.annotations.NotNull Connection conn, String macAdress) {

        List<List<Object>> allComponentes = new ArrayList<>();


        String sql = """
                SELECT cp.fkComponente , m.min, m.max,cp2.nome
                from componente_mainframe as cp
                JOIN metrica m on m.id = cp.fkMetrica and m.fkComponente = cp.fkComponente\s
                join tipo t on m.fkTipo = t.id
                join componente cp2 on cp.fkComponente=cp2.id
                WHERE fkMainframe = (SELECT id FROM mainframe WHERE macAdress = ?) ;
                """;


        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, macAdress);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Integer componente = rs.getInt("fkcomponente");
                    Double min = rs.getDouble("min");
                    Double max = rs.getDouble("max");
                    String nome=rs.getString("nome");

                    List<Object> comp_met = new ArrayList<>();
                    comp_met.add(componente);
                    comp_met.add(min);
                    comp_met.add(max);
                    comp_met.add(nome);

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
