const express = require('express');
const cors = require('cors');
require('dotenv').config();
const db = require('./db');

const app = express();
const PORT = process.env.PORT || 4720;

app.use(cors());
app.use(express.json());

app.get('/api/residents', async (req, res) => {
    try {
        const [rows] = await db.query("select * from resident;");
        res.json({
            server: 'Running',
            database: 'Connected',
            db_test_result: rows[0].name
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ server: 'Running', database: 'Disconnected', error: error.message });
    }
});

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});