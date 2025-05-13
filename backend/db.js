const oracledb = require('oracledb');

const dbConfig = {
    user: "C##FOODIE_USER",
    password: " ",
    connectString: " "
};


async function initialize() {
    try {
        await oracledb.createPool({
            ...dbConfig,
            poolMin: 2,
            poolMax: 10,
            poolIncrement: 2
        });
        console.log(" Oracle DB Connection Pool Initialized");
    } catch (err) {
        console.error(" Oracle DB Connection Error: ", err);
    }
}

async function execute(query, binds = [], options = { autoCommit: true }) {
    let connection;
    try {
        // 🔹 Get connection from the pool
        connection = await oracledb.getConnection();
        const result = await connection.execute(query, binds, options);
        return result;
    } catch (err) {
        console.error(" Query Execution Error: ", err);
        throw err;
    } finally {
        if (connection) {
            try {
                await connection.close();
            } catch (err) {
                console.error(" Error Closing Connection: ", err);
            }
        }
    }
}

module.exports = { initialize, execute };
