package sptech.school;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnectionDb {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        // Conectar ao banco
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conexão estabelecida com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro na conexão: " + e.getMessage());
        }

    }

    public static void inserirAlerta(Connection conn,
                                     String dtHora,
                                     String descricao,
                                     Double valorColetado,
                                     String macAdress
                                     ) {

        String sql = """
            INSERT INTO alerta (dt_hora, descricao, valor_coletado, fkMainframe)
            VALUES (?, ?, ?, (SELECT id FROM mainframe WHERE numeroDeSerie = ?))
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

}
