import express, { NextFunction, Request, Response } from "express";
import { validateToken } from "../tokenManager";
import { authLogin, authLogout, authRegister } from "./auth";

const authController = express.Router();

// /auth/register
function validateRegisterRequest(req: Request, res: Response, next: NextFunction) {
	const { email, name, password } = req.body;
	if (email === undefined || name === undefined || password === undefined) {
		return res.status(400).json({ message: "Error: Malformed response." });
	}
	next();
}

authController.post("/register", validateRegisterRequest, async (req: Request, res: Response) => {
	const { email, name, password } = req.body;
	const reply = await authRegister(email, name, password);
	res.status(reply.code).json({ message: reply.message });
});

// /auth/login
function validateLoginRequest(req: Request, res: Response, next: NextFunction) {
	const { email, password } = req.body;
	if (email === undefined || password === undefined) {
		return res.status(400).json({ message: "Error: Malformed response." });
	}
	next();
}

authController.post("/login", validateLoginRequest, async (req: Request, res: Response) => {
	const { email, password } = req.body;
	const reply = await authLogin(email, password);
	res.status(reply.code).json({ message: reply.message });
});

// /auth/logout
authController.post("/logout", async (req: Request, res: Response) => {
	const token = req.header("token") as string;
	if (validateToken(token) !== undefined) {
		authLogout(token);
	}
	res.json();
});

export { authController };