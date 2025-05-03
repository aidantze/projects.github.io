// Imports
import cors from "cors";
import express, { json, Request, Response } from "express";
import morgan from "morgan";
import config from "./config.json";

// Import endpoints
import { authController } from "./auth/authController";
import { invoiceController } from "./invoice/invoiceController";
import { pool } from "./dbPool";

// Set up web app
const app = express();
app.use(cors());
app.use(json());
app.use(morgan("dev"));

const PORT: number = parseInt(process.env.PORT || config.port);
const HOST: string = process.env.IP || "localhost";

/**
 * Endpoint: '/healthcheck'
 * Method: GET
 * Parameter: ()
 * Output: status: str
 *
 * Perform a health check to show the "aliveness" of the service
 */
app.get("/healthcheck", (req, res) => {
	const statusCode = 200;
	/*
  if (userdb.connect() === "Successful") {
    statusCode = 200;    // available
  } else {
    statusCode = 500;    // unavailable
  }
  */
	res.json({ statusCode });
});

// Root URL
app.get("/", (req: Request, res: Response) => {
	res.json(
		{
			message: "Hello world!",
		}
	);
});

// Endpoints are passed through routers, reducing clutter in server.ts
app.use("/auth", authController);
app.use("/invoice", invoiceController);

const server = app.listen(PORT, HOST, () => {
	console.log(`⚡️ Server listening on port ${PORT} at ${HOST}`);
});

// For coverage, handle Ctrl+C gracefully
process.on("SIGINT", () => {
	pool.end();
	server.close(() => console.log("Shutting down server gracefully."));
});