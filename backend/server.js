const express = require('express');
const bodyParser = require('body-parser');
const db = require('./db'); // 🔹 Import the db.js file

const app = express();
const PORT = 1234;
const oracledb = require('oracledb');

app.use(bodyParser.json());

// Initialize DB Pool
db.initialize();

const cors = require('cors');
app.use(cors());

app.use(bodyParser.json({ limit: '10mb' })); // or even '20mb' if needed
app.use(bodyParser.urlencoded({ limit: '10mb', extended: true }));

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

            const formattedUser = {
                username: user[1], // Assuming USERNAME is at index 1
                email: user[2],    // Assuming EMAIL is at index 2
                // skip password intentionally for security
            };

            return res.status(200).json({ message: "Login successful!", user: formattedUser });

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


app.post('/loginOwner', async (req, res) => {
    try {
        const { email, password } = req.body;

        console.log("🔍 Received login request for:", email);

        if (!email || !password) {
            return res.status(400).json({ message: "All fields are required" });
        }

        // 🔹 Fetch owner from DB (using the correct 'owners' table)
        const result = await db.execute(
            `SELECT * FROM owners WHERE EMAIL = :email`,
            { email }
        );

        console.log("🔍 DB Query Result:", result);

        if (result.rows && Array.isArray(result.rows) && result.rows.length > 0) {
            const owner = result.rows[0];

            console.log("🔍 Retrieved Owner:", owner);

            const dbPassword = owner[5]?.trim();  // Assuming PASSWORD is the 5th column (index 4)
            const inputPassword = password.trim();

            console.log("🔍 Comparing passwords:", dbPassword, inputPassword);

            if (dbPassword !== inputPassword) {
                console.log(" Password mismatch");
                return res.status(401).json({ message: "Invalid credentials" });
            }

            const formattedOwner = {
                owner_name: owner[1],           // OWNER_NAME
                restaurant_name: owner[2],      // RESTAURANT_NAME
                email: owner[3],                // EMAIL
                // password skipped for security
            };

            return res.status(200).json({ message: "Login successful!", owner: formattedOwner });

        } else {
            console.log(" Owner not found");
            return res.status(401).json({ message: "Invalid credentials" });
        }

    } catch (error) {
        console.error(" Login Error:", error);
        res.status(500).json({ message: "Server error" });
    }
});


app.post('/signupOwner', async (req, res) => {
    try {
        const { owner_name, restaurant_name, email, password, location } = req.body;

        console.log("🔍 Received signup request for:", owner_name, email);

        if (!owner_name || !restaurant_name || !email || !password || !location) {
            return res.status(400).json({ message: "All fields are required" });
        }

        // 🔹 Check if owner already exists
        const existingOwner = await db.execute(
            `SELECT * FROM owners WHERE EMAIL = :email`,
            { email }
        );

        if (existingOwner.rows && existingOwner.rows.length > 0) {
            console.log("️ Email already registered");
            return res.status(409).json({ message: "Email already registered" });
        }

        // 🔹 Insert new owner into the database
        const insertQuery = `
            INSERT INTO owners (OWNER_NAME, RESTAURANT_NAME, EMAIL, PASSWORD, LOCATION)
            VALUES (:owner_name, :restaurant_name, :email, :password, :location)
        `;
        const insertResult = await db.execute(insertQuery, { owner_name, restaurant_name, email, password, location });

        console.log(" Owner registered successfully:", insertResult);

        return res.status(201).json({
            message: "Signup successful!",
            user: { owner_name, restaurant_name, email, location }
        });

    } catch (error) {
        console.error(" Signup Error:", error);
        res.status(500).json({ message: "Server error" });
    }
});


app.post('/addItem', async (req, res) => {
    try {
        const { item_name, item_price, item_description, item_ingredients, item_imageBase64 } = req.body;

        console.log("Received item:", item_name, item_price);

        if (!item_name || !item_price || !item_description || !item_ingredients || !item_imageBase64) {
            return res.status(400).json({ success: false, message: "All fields required" });
        }

        const imageBuffer = Buffer.from(item_imageBase64, 'base64');

        const insertQuery = `
            INSERT INTO FOOD_ITEMS (NAME, PRICE, DESCRIPTION, INGREDIENTS, IMAGE)
            VALUES (:item_name, :item_price, :item_description, :item_ingredients, :item_imageBase64)
        `;

        await db.execute(insertQuery, {
            item_name,
            item_price,
            item_description,
            item_ingredients,
            item_imageBase64: imageBuffer
        }, { autoCommit: true });

        res.status(201).json({ success: true, message: "Item added successfully!" });

    } catch (error) {
        console.error("Error adding item:", error);
        res.status(500).json({ success: false, message: "Server error" });
    }
});

app.get('/getAllFoodItems', async (req, res) => {
  try {
    const result = await db.execute(
      `SELECT ID, NAME, PRICE, IMAGE FROM FOOD_ITEMS`,
      [], // no binds
      { outFormat: oracledb.OUT_FORMAT_OBJECT } // optional: use object instead of array
    );

    const items = await Promise.all(result.rows.map(async (row) => {
      const id = row.ID;
      const name = row.NAME;
      const price = row.PRICE;
      const imageBlob = row.IMAGE;

      let imageBase64 = '';
      if (imageBlob) {
        const buffer = await imageBlob.getData(); // oracledb >=6 only
        imageBase64 = buffer.toString('base64');
      }

      return { id, name, price, imageBase64 };
    }));

    res.json(items);

  } catch (err) {
    console.error('Error fetching items:', err);
    res.status(500).send('Failed to fetch items');
  }
});

app.get('/getFoodItemById', async (req, res) => {
  const foodId = req.query.id;

  if (!foodId) {
    return res.status(400).send('Missing food item ID');
  }

  try {
    const result = await db.execute(
      `SELECT ID, NAME, PRICE, DESCRIPTION, INGREDIENTS, IMAGE FROM FOOD_ITEMS WHERE ID = :id`,
      [foodId],
      { outFormat: oracledb.OUT_FORMAT_OBJECT }
    );

    const row = result.rows[0];

    if (!row) {
      return res.status(404).send('Food item not found');
    }

    const { ID, NAME, PRICE, DESCRIPTION, INGREDIENTS, IMAGE } = row;

    let imageBase64 = '';
    if (IMAGE) {
      const buffer = await IMAGE.getData(); // Only works with oracledb >= 6
      imageBase64 = buffer.toString('base64');
    }

    const foodItem = {
      id: ID,
      name: NAME,
      price: PRICE,
      description: DESCRIPTION,
      ingredients: INGREDIENTS,
      imageBase64: imageBase64
    };

    res.json(foodItem);

  } catch (err) {
    console.error('Error fetching food item by ID:', err);
    res.status(500).send('Failed to fetch food item');
  }
});

app.post('/cart/add', async (req, res) => {
  const { userId, foodId } = req.body;

  if (!userId || !foodId) {
    return res.status(400).json({ success: false, message: 'Missing userId or foodId' });
  }

  try {
    await db.execute(
      `INSERT INTO CART (user_id, food_id) VALUES (:userId, :foodId)`,
      { userId: parseInt(userId), foodId: parseInt(foodId) }, // Type-safety
      { autoCommit: true }
    );

    res.json({ success: true, message: 'Item added to cart' });

  } catch (err) {
    console.error('Error adding item to cart:', err);
    res.status(500).json({ success: false, message: 'Failed to add item to cart' });
  }
});


// Start Server
const server = app.listen(1234, '192.168.1.3', () => {
    console.log(` Server running on http://192.168.1.3:1234`);
});
