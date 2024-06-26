package org.strassburger.lifestealz.util.storage;

import org.strassburger.lifestealz.LifeStealZ;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLitePlayerDataStorage implements PlayerDataStorage {
    @Override
    public void init() {
        try (Connection connection = createConnection()) {
            if (connection == null) return;
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS hearts (uuid TEXT PRIMARY KEY, name TEXT, maxhp REAL, hasbeenRevived INTEGER, craftedHearts INTEGER, craftedRevives INTEGER, killedOtherPlayers INTEGER)");
            } catch (SQLException e) {
                LifeStealZ.getInstance().getLogger().severe("Failed to initialize SQLite database: " + e.getMessage());
            }
        } catch (SQLException e) {
            LifeStealZ.getInstance().getLogger().severe("Failed to initialize SQLite database: " + e.getMessage());
        }
    }

    private Connection createConnection() {
        try {
            LifeStealZ plugin = LifeStealZ.getInstance();
            String pluginFolderPath = plugin.getDataFolder().getPath();
            return DriverManager.getConnection("jdbc:sqlite:" + pluginFolderPath + "/userData.db");
        } catch (SQLException e) {
            LifeStealZ.getInstance().getLogger().severe("Failed to create connection to SQLite database: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void save(PlayerData playerData) {
        try (Connection connection = createConnection()) {
            if (connection == null) return;
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT OR REPLACE INTO hearts (uuid, name, maxhp, hasbeenRevived, craftedHearts, craftedRevives, killedOtherPlayers) VALUES ('" + playerData.getUuid() + "', '" + playerData.getName() + "', " + playerData.getMaxhp() + ", " + playerData.getHasbeenRevived() + ", " + playerData.getCraftedHearts() + ", " + playerData.getCraftedRevives() + ", " + playerData.getKilledOtherPlayers() + ")");
            } catch (SQLException e) {
                LifeStealZ.getInstance().getLogger().severe("Failed to save player data to SQLite database: " + e.getMessage());
            }
        } catch (SQLException e) {
            LifeStealZ.getInstance().getLogger().severe("Failed to save player data to SQLite database: " + e.getMessage());
        }
    }

    @Override
    public PlayerData load(UUID uuid) {
        try (Connection connection = createConnection()) {
            if (connection == null) return null;
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(30);
                try (ResultSet resultSet = statement.executeQuery("SELECT * FROM hearts WHERE uuid = '" + uuid + "'");) {
                    if (!resultSet.next()) return null;
                    PlayerData playerData = new PlayerData(resultSet.getString("name"), uuid);
                    playerData.setMaxhp(resultSet.getDouble("maxhp"));
                    playerData.setHasbeenRevived(resultSet.getInt("hasbeenRevived"));
                    playerData.setCraftedHearts(resultSet.getInt("craftedHearts"));
                    playerData.setCraftedRevives(resultSet.getInt("craftedRevives"));
                    playerData.setKilledOtherPlayers(resultSet.getInt("killedOtherPlayers"));
                    return playerData;
                } catch (SQLException e) {
                    LifeStealZ.getInstance().getLogger().severe("Failed to load player data from SQLite database: " + e.getMessage());
                    return null;
                }
            } catch (SQLException e) {
                LifeStealZ.getInstance().getLogger().severe("Failed to load player data from SQLite database: " + e.getMessage());
                return null;
            }
        } catch (SQLException e) {
            LifeStealZ.getInstance().getLogger().severe("Failed to load player data from SQLite database: " + e.getMessage());
            return null;
        }
    }

    @Override
    public PlayerData load(String uuid) {
        return load(UUID.fromString(uuid));
    }

    @Override
    public List<UUID> getEliminatedPlayers() {
        List<UUID> eliminatedPlayers = new ArrayList<>();

        try (Connection connection = createConnection()) {
            if (connection == null) return eliminatedPlayers;

            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(30);

                ResultSet resultSet = statement.executeQuery("SELECT uuid FROM hearts WHERE maxhp <= 0.0");

                while (resultSet.next()) {
                    eliminatedPlayers.add(UUID.fromString(resultSet.getString("uuid")));
                }
            } catch (SQLException e) {
                LifeStealZ.getInstance().getLogger().severe("Failed to load player data from SQLite database: " + e.getMessage());
            }
        } catch (SQLException e) {
            LifeStealZ.getInstance().getLogger().severe("Failed to load player data from SQLite database: " + e.getMessage());
        }

        return eliminatedPlayers;
    }
}
