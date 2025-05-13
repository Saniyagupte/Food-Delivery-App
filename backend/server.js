const express = require('express');
const bodyParser = require('body-parser');
const session = require('express-session');
const db = require('./db'); // 🔹 Import the db.js file
const cors = require('cors');
const axios = require('axios');


const app = express();
const PORT = 1234;
const oracledb = require('oracledb');

const unsplashApiKey = 'L7BaYbAzk-751sMAmoZEGdOXVZfdAwWC92YNcso-Cf0';
app.use(express.json());
app.use(cors());

const CO_API_KEY = 'A2rbRuuzQCrdqROsWeIofSuWR9MkhZQqnGmI6coq';

const { CohereClient } = require('cohere-ai');

const cohereClient = new CohereClient({
  token: CO_API_KEY // 🔹 NOT "apiKey" — must use "token"
});


console.log("Cohere Client Initialized with:", CO_API_KEY.substring(0, 5), "***"); // Log the first few characters of the key

app.use(session({
    secret: process.env.SESSION_SECRET || 'your_secret_key',  // Use the secret key, typically from .env file
    resave: false,  // Don't save session if it's not modified
    saveUninitialized: true,  // Save uninitialized sessions (new sessions)
    cookie: { secure: false }  // Set to true if you're using HTTPS
}));

app.use(bodyParser.json({ limit: '10mb' }));
app.use(bodyParser.urlencoded({ limit: '10mb', extended: true }));

// Add request logging middleware
app.use((req, res, next) => {
    console.log(`${req.method} ${req.url}`);
    console.log('Headers:', req.headers);
    console.log('Body:', req.body);
    next();
});

// Initialize DB Pool
db.initialize();


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

            // ✅ Store userId in session
            req.session.userId = user[0];  // Assuming user[0] is the userId
            console.log("🔍 Session userId set:", req.session.userId);

            // ✅ Send back user details including userId
            const formattedUser = {
                id: user[0],    // userId
                name: user[1],   // Assuming USERNAME is at index 1
                email: user[2],      // Assuming EMAIL is at index 2
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

        if (!item_name || !item_price || !item_description || !item_ingredients) {
            return res.status(400).json({ success: false, message: "Name, price, description, and ingredients are required" });
        }

        const insertQuery = `
            INSERT INTO FOOD_ITEMS (NAME, PRICE, DESCRIPTION, INGREDIENTS, IMAGE)
            VALUES (:item_name, :item_price, :item_description, :item_ingredients, :item_imageBase64)
        `;

        const params = {
            item_name,
            item_price,
            item_description,
            item_ingredients,
            item_imageBase64: item_imageBase64 ? Buffer.from(item_imageBase64, 'base64') : null
        };

        await db.execute(insertQuery, params, { autoCommit: true });

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
        const buffer = await imageBlob.getData();
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
    const { foodId, userId } = req.body;

    // Input validation
    if (typeof userId === 'undefined' || typeof foodId === 'undefined') {
        return res.status(400).json({ success: false, message: 'Missing userId or foodId' });
    }
    // Validate and parse foodId immediately
    const parsedFoodId = parseInt(foodId, 10);
    if (isNaN(parsedFoodId)) {
         return res.status(400).json({ success: false, message: 'Invalid foodId format' });
    }


    try {
        // 1. Execute the SELECT query and get the result object
        const selectResult = await db.execute(
            `SELECT quantity FROM CART WHERE user_id = :userId AND food_id = :foodId`,
            // Ensure binds use correct types (assuming userId is already number, foodId is parsed)
            { userId: userId, foodId: parsedFoodId }
        );

        // 2. Log the structure of the result (Optional but helpful for debugging)
        // console.log('SELECT query result structure:', selectResult);

        // 3. Access the 'rows' array safely from the result object
        const existingRows = selectResult && selectResult.rows ? selectResult.rows : [];

        // 4. Check the length of the 'rows' array to see if item exists
        if (existingRows.length > 0) {
            // Item exists - Update quantity
            console.log(`Item exists for userId: ${userId}, foodId: ${parsedFoodId}. Updating quantity.`);
            await db.execute(
                `UPDATE CART SET quantity = quantity + 1 WHERE user_id = :userId AND food_id = :foodId`,
                { userId: userId, foodId: parsedFoodId }, // Use parsed values
                { autoCommit: true } // Ensure autoCommit is handled by your db config or needed here
            );
            console.log(`🆙 Quantity updated for userId: ${userId}, foodId: ${parsedFoodId}`);
            res.json({ success: true, message: 'Cart quantity updated' });
        } else {
            // Item does not exist - Insert new item
            console.log(`Item does not exist for userId: ${userId}, foodId: ${parsedFoodId}. Inserting new item.`);
            await db.execute(
                `INSERT INTO CART (user_id, food_id, quantity) VALUES (:userId, :foodId, 1)`,
                { userId: userId, foodId: parsedFoodId }, // Use parsed values
                { autoCommit: true } // Ensure autoCommit is handled by your db config or needed here
            );
            console.log(`🆕 Item inserted into cart for userId: ${userId}, foodId: ${parsedFoodId}`);
            res.json({ success: true, message: 'Item added to cart' });
        }
    } catch (err) {
        // Log context for better error tracking
        console.error(`❌ Error adding/updating item in cart for userId: ${userId}, foodId: ${foodId}:`, err);
        res.status(500).json({ success: false, message: 'Failed to add/update item in cart', error: err.message });
    }
});



// Ensure you have the necessary imports/setup for your 'db' object (e.g., require('oracledb'))

// Ensure you have the necessary imports/setup for your 'db' object (e.g., require('oracledb'))

app.get('/cart/:userId', async (req, res) => {
    const userIdParam = req.params.userId;
    console.log(`Received request for cart for userId param: ${userIdParam}`);

    const userId = parseInt(userIdParam, 10);
    if (isNaN(userId)) {
        console.error("Invalid userId parameter:", userIdParam);
        return res.status(400).json({ message: "Invalid user ID format." });
    }
    console.log(`Parsed userId as integer: ${userId}`);

    try {
        // Original SQL Query (select more fields if needed: c.id, f.description, f.ingredients)
        const sqlQuery =
            `SELECT f.id, f.name, f.price, f.image, c.quantity
             FROM cart c
             JOIN food_items f ON c.food_id = f.id
             WHERE c.user_id = :userId`;

        console.log(`Executing SQL for userId: ${userId}`);
        const result = await db.execute(sqlQuery, { userId: userId });
        console.log(`SQL query returned ${result.rows ? result.rows.length : 0} rows.`);

        if (!result.rows || result.rows.length === 0) {
            console.log(`No cart items found for userId: ${userId}`);
            return res.status(200).json([]); // Return empty array for no items
        }

        const cartItems = await Promise.all(result.rows.map(async (row) => {
            let imageBase64 = null;
            const imageBlob = row[3]; // Index 3 = f.image

            if (imageBlob) {
                try {
                    const buffer = await imageBlob.getData();
                    if (buffer instanceof Buffer) {
                        imageBase64 = buffer.toString('base64');
                    }
                } catch (imageError) {
                    console.error(`Error processing image for food_id ${row[0]}:`, imageError);
                }
            }

            return {
                foodItem: {
                    id: row[0],        // f.id
                    name: row[1],      // f.name
                    price: row[2],     // f.price
                    imageBase64: imageBase64
                },
                quantity: row[4]       // c.quantity
            };
        }));

        console.log(`Successfully processed ${cartItems.length} cart items for userId: ${userId}`);
        res.status(200).json(cartItems); // Send the array of nested objects

    } catch (error) {
        console.error(`Error fetching cart items for userId ${userId}:`, error);
        res.status(500).json({ message: "Server error while fetching cart items." });
    }
});

app.post('/cart/increase', async (req, res) => {
    const { foodId, userId } = req.body;

    const parsedFoodId = parseInt(foodId, 10);
    if (isNaN(parsedFoodId)) {
        return res.status(400).json({ success: false, message: 'Invalid foodId format' });
    }

    try {
        const selectResult = await db.execute(
            `SELECT quantity FROM CART WHERE user_id = :userId AND food_id = :foodId`,
            { userId, foodId: parsedFoodId }
        );

        const existingRows = selectResult.rows || [];
        if (existingRows.length > 0) {
            await db.execute(
                `UPDATE CART SET quantity = quantity + 1 WHERE user_id = :userId AND food_id = :foodId`,
                { userId, foodId: parsedFoodId },
                { autoCommit: true }
            );
            res.json({ success: true, message: 'Quantity increased' });
        } else {
            await db.execute(
                `INSERT INTO CART (user_id, food_id, quantity) VALUES (:userId, :foodId, 1)`,
                { userId, foodId: parsedFoodId },
                { autoCommit: true }
            );
            res.json({ success: true, message: 'Item added to cart' });
        }
    } catch (err) {
        res.status(500).json({ success: false, message: 'Error updating cart', error: err.message });
    }
});

app.post('/cart/decrease', async (req, res) => {
    const { userId, foodId } = req.body;

    try {
        await db.execute(
            `UPDATE CART SET quantity = quantity - 1
             WHERE user_id = :userId AND food_id = :foodId AND quantity > 1`,
            { userId, foodId },
            { autoCommit: true }
        );
        res.json({ success: true, message: 'Quantity decreased' });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Error decreasing quantity', error: err.message });
    }
});

app.post('/cart/delete', async (req, res) => {
    const { userId, foodId } = req.body;

    try {
        await db.execute(
            `DELETE FROM CART WHERE user_id = :userId AND food_id = :foodId`,
            { userId, foodId },
            { autoCommit: true }
        );
        res.json({ success: true, message: 'Item removed from cart' });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Error deleting item', error: err.message });
    }
});

app.post('/orders/place', async (req, res) => {
    console.log("Received order:", JSON.stringify(req.body));

    const { userId, orderItems } = req.body;

    if (!userId || !orderItems || orderItems.length === 0) {
        console.log("Missing userId or orderItems:", { userId, orderItems });
        return res.status(400).json({
            message: 'Invalid data. UserId and cart items are required.',
            success: false
        });
    }

    const insertQuery = `
        INSERT INTO PLACE_ORDER (user_id, food_id, quantity)
        VALUES (:userId, :foodId, :quantity)
    `;

    try {
        console.log("Preparing to insert order items:", orderItems);

        for (const item of orderItems) {
            const { foodId, quantity } = item;

            if (!Number.isInteger(foodId) || foodId <= 0 || !Number.isInteger(quantity) || quantity <= 0) {
                return res.status(400).json({
                    message: 'Invalid foodId or quantity in order items.',
                    success: false
                });
            }

            console.log(`Inserting item: foodId = ${foodId}, quantity = ${quantity}`);

            await db.execute(
                insertQuery,
                { userId, foodId, quantity },
                { autoCommit: true } // Commit immediately
            );

            console.log(`Successfully inserted item: foodId = ${foodId}`);
        }

        console.log(`Successfully placed ${orderItems.length} orders for userId: ${userId}`);
        return res.status(200).json({ message: 'Order placed successfully', success: true });

    } catch (error) {
        console.error('Error placing order:', error);
        return res.status(500).json({ message: 'Server error while placing order', success: false });
    }
});


const getIngredientImages = async (ingredients) => {
  try {
    const images = await Promise.all(
      ingredients.map(async (ingredient) => {
        const response = await axios.get(
          `https://api.unsplash.com/photos/random?query=${ingredient}&client_id=${unsplashApiKey}`
        );
        return response.data.urls.small;
      })
    );
    return images;
  } catch (error) {
    console.error("Error fetching images: ", error.message);
    return [];
  }
};

app.post('/generate-recipe', async (req, res) => {
  const { query } = req.body;

  try {
    const response = await cohereClient.generate({
      model: 'command',
      prompt: `Give me a recipe for ${query}`,
      max_tokens: 100,
      temperature: 0.7
    });

    if (response.body && response.body.generations && response.body.generations[0]) {
      const recipeText = response.body.generations[0].text;
      res.status(200).json({ recipe: recipeText });
    } else {
      console.error('Unexpected response format:', response.body);
      res.status(500).json({ message: 'Error: No valid recipe found.' });
    }
  } catch (error) {
    console.error('Error generating recipe:', error);
    res.status(500).json({ message: `Error generating recipe: ${error.message}` });
  }
});

function extractIngredientsSimpleNames(recipeText) {
  const ingredients = recipeText
    .split('\n')
    .map(line => line.trim())
    .filter(line => line !== '' && /^\d+\.\s/.test(line)) // Filter lines starting with a number and a dot
    .map(line => line.replace(/^\d+\.\s/, '').trim()); // Remove the number and dot, then trim
  return ingredients;
}


// *** IMPLEMENTING THE /search-recipes GET ENDPOINT ***
app.get('/search-recipes', async (req, res) => {
  const { query } = req.query;

  if (!query) {
    return res.status(400).json({ message: 'Error: Search query is required.' });
  }

  try {
    const prompt = `Give me only list of ingredients used for making "${query}". Please list all the individual ingredients ONLY, in form of list, one by one`;

    const response = await cohereClient.generate({
      model: 'command',
      prompt: prompt,
      max_tokens: 300,
      temperature: 0.5
    });
    console.log('🧠 Raw Cohere response (detailed recipe):', response);

        if (response && response.generations && response.generations[0]) {
          const recipeText = response.generations[0].text;
          const ingredientsList = extractIngredientsSimpleNames(recipeText); // Use the new function

          const firstRecipe = {
            name: query,
            ingredients: ingredientsList
          };

          res.status(200).json({ recipe: [firstRecipe] });

    } else {
      console.error('Unexpected response format from Cohere (detailed recipe):', response.body);
      res.status(500).json({ message: 'Error: No valid recipe found from AI.' });
    }

  } catch (error) {
    console.error('Error generating detailed recipe:', error);
    res.status(500).json({ message: `Error generating detailed recipe: ${error.message}` });
  }
});

// Start Server
const server = app.listen(1234, '192.168.0.103', () => {
    console.log(` Server running on http://192.168.0.103:1234`);
});
