package sptech.school;

import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static sptech.school.JiraIntegration.abrirChamado;

public class ConnectionDb {

    public static Connection conectar() throws SQLException {
        Dotenv dotenv = Dotenv.load();
        return DriverManager.getConnection(
                dotenv.get("DB_URL"),
                dotenv.get("DB_USER"),
                dotenv.get("DB_PASSWORD")
        );
    }

    /**
     * Insere um alerta no banco de dados Synkro.
     * A gravidade e o status padrão são 1 (poderão ser alterados por trigger futuramente).
     */
    public static void inserirAlerta(@NotNull Connection conn,
                                     String dtHora, Integer fkComponente, Object valorColetado,
                                     String macAdress, String nomeComponente) {

        String sql = """
            INSERT INTO alerta (dt_hora, fkComponente, valor_coletado, fkMainframe, fkGravidade, fkStatus)
            VALUES (?, ?, ?, 
                    (SELECT id FROM mainframe WHERE macAdress = ? LIMIT 1),
                    1, 1)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dtHora);
            stmt.setInt(2, fkComponente);
            stmt.setObject(3, valorColetado);
            stmt.setString(4, macAdress);

            stmt.executeUpdate();
            System.out.println("🚨 Alerta inserido no Synkro: " + nomeComponente);

            abrirChamado("ERRO no " + nomeComponente,
                    "Valor fora do limite: " + valorColetado);

        } catch (SQLException e) {
            System.err.println("❌ Erro ao inserir alerta: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("⚠️ Erro ao abrir chamado no Jira: " + e.getMessage());
        }
    }


    public static List<List<Object>> buscarMetricas(Connection conn, String macAdress) throws SQLException {
        String sql = """
            SELECT 
                cm.fkComponente,
                mt.min,
                mt.max,
                c.nome AS nomeComponente,
                nm.nome AS nomeMetrica
            FROM componente_mainframe cm
            JOIN metrica mt ON mt.fkComponente = cm.fkComponente
            JOIN nome_metrica nm ON nm.id = mt.fkNomeMetrica
            JOIN componente c ON c.id = cm.fkComponente
            JOIN mainframe mf ON mf.id = cm.fkMainframe
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
                        rs.getString("nomeComponente"),
                        rs.getString("nomeMetrica")
                ));
            }
        }

        return lista;
    }
}
