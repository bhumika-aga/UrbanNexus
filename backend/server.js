const express = require('express');
const cors = require('cors');
require('dotenv').config();
const db = require('./db');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const authenticateToken = require('./authMiddleware');

const app = express();
const PORT = process.env.PORT || 4720;

app.use(cors());
app.use(express.json());

// User Login
app.post('/api/login', async (req, res) => {
    const { username, password } = req.body;

    try {

        const [admins] = await db.query('SELECT * FROM admin WHERE username = ?', [username]);

        if (admins.length === 0) {
            return res.status(401).json({ error: 'Invalid username or password' });
        }

        const admin = admins[0];

        const isMatch = await bcrypt.compare(password, admin.password_hash);

        if (!isMatch) {
            return res.status(401).json({ error: 'Invalid username or password' });
        }

        const token = jwt.sign(
            { admin_id: admin.admin_id, role: admin.role },
            process.env.JWT_SECRET,
            { expiresIn: '2h' }
        );

        res.json({
            message: 'Login successful!',
            token: token,
            admin: {
                username: admin.username,
                role: admin.role
            }
        });

    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'Technical Issue will be back in some time' });
    }
});

// Get Residents
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

// Add Resident
app.post('/api/residents', authenticateToken, async (req, res) => {
    const { name, house_block, house_floor, house_unit, ownership_status, contact, no_of_members } = req.body;

    try {
        const [result] = await db.query(
            'INSERT INTO resident (name, house_block, house_floor, house_unit, ownership_status, contact, no_of_members) VALUES (?, ?, ?, ?, ?, ?, ?)',
            [name, house_block, house_floor, house_unit, ownership_status, contact, no_of_members]
        );
        res.status(201).json({ message: 'Resident added successfully!', resident_id: result.insertId });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: 'Failed to add resident' });
    }
});

// Add Technician
app.post('/api/technicians', authenticateToken, async (req, res) => {
    const { tech_id, name, contact, skill } = req.body;

    try {
        await db.query(
            'INSERT INTO technician (tech_id, name, contact, skill) VALUES (?, ?, ?, ?)',
            [tech_id, name, contact, skill]
        );
        res.status(201).json({ message: 'Technician added successfully!' });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: 'Failed to add technician' });
    }
});

// Add Amenity
app.post('/api/amenities', authenticateToken, async (req, res) => {
    const { amenity_id, name, capacity } = req.body;

    try {
        await db.query(
            'INSERT INTO amenity (amenity_id, name, capacity) VALUES (?, ?, ?)',
            [amenity_id, name, capacity]
        );
        res.status(201).json({ message: 'Amenity added successfully!' });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: 'Failed to add amenity' });
    }
});

// Book Technician
app.post('/api/bookings/technician', authenticateToken, async (req, res) => {

    const { resident_id, skill, slot, assign_date } = req.body;

    try {
        const [results] = await db.query(
            'CALL AutoBookTechnician(?, ?, ?, ?)',
            [resident_id, skill, slot, assign_date]
        );

        const invoiceData = results[0][0];

        res.status(201).json({
            message: 'Technician booked successfully!',
            invoice: invoiceData
        });

    } catch (error) {
        console.error('Booking Error:', error);
        if (error.sqlState === '45000') {
            return res.status(400).json({ error: error.message });
        }
        res.status(500).json({ error: 'Failed to book technician.' });
    }
});

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});