const express = require('express');
const bodyParser = require('body-parser');
const db = require('./db'); // 🔹 Import the db.js file

const app = express();
const PORT = 1234;

app.use(bodyParser.json());

// Initialize DB Pool
db.initialize();

const cors = require('cors');
app.use(cors());

// Login page route
app.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        console.log("🔍 Received login request for:", email, password);  // Debugging

        if (!email || !password) {
            return res.status(400).json({ message: "All fields are required" });
        }

        // 🔹 Fetch user from DB
        const result = await db.execute(
            `SELECT * FROM users WHERE EMAIL = :email`,
            { email: email }  // Bind the email parameter correctly
        );

        // Log the result for debugging
        console.log("🔍 DB Query Result:", result);

        // Ensure `result.rows` exists and is an array
        if (result.rows && Array.isArray(result.rows) && result.rows.length > 0) {
            const user = result.rows[0]; // Get the first user record

            console.log("🔍 Retrieved User:", user);

            // Trim both the database password and input password to remove any hidden spaces
            const dbPassword = user[3].trim(); // Assuming the password is in the 4th position
            const inputPassword = password.trim();

            console.log("🔍 Comparing passwords: DB Password:", dbPassword, "Input Password:", inputPassword);

            // Check if passwords match
            if (dbPassword !== inputPassword) {
                console.log(" Password mismatch");
                return res.status(401).json({ message: "Invalid credentials" });
            }

            return res.status(200).json({ message: "Login successful!", user });
        } else {
            console.log(" User not found");
            return res.status(401).json({ message: "User not found" });
        }
    } catch (error) {
        console.error(" Login Error:", error);
        res.status(500).json({ message: "Server error" });
    }
});

// Signup page route
// Signup page route
app.post('/signup', async (req, res) => {
    try {
        const { name, email, password } = req.body;

        console.log("🔍 Received signup request for:", name, email, password);  // Debugging

        if (!name || !email || !password) {
            return res.status(400).json({ message: "All fields are required" });
        }

        // 🔹 Check if user already exists
        const existingUser = await db.execute(
            `SELECT * FROM users WHERE EMAIL = :email`,
            { email: email }  // Bind the email parameter correctly
        );

        console.log("🔍 Checking if email already exists:", existingUser);

        if (existingUser.rows && existingUser.rows.length > 0) {
            console.log(" Email already registered");
            return res.status(409).json({ message: "Email already registered" });
        }

        // 🔹 Insert new user into the database
        const insertQuery = `INSERT INTO users (USERNAME, EMAIL, PASSWORD) VALUES (:name, :email, :password)`;
        const insertResult = await db.execute(insertQuery, { name, email, password });

        console.log(" User registered successfully:", insertResult);

        return res.status(201).json({ message: "Signup successful!", user: { name, email } });

    } catch (error) {
        console.error(" Signup Error:", error);
        res.status(500).json({ message: "Server error" });
    }
});




// Start Server
const server = app.listen(1234, '0.0.0.0', () => {
    console.log(` Server running on http://0.0.0.0:1234`);
});
