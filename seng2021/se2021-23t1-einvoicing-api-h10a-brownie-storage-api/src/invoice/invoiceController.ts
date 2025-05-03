import express, { NextFunction, Request, Response } from "express";
import { validateToken } from "../tokenManager";
import { insert, invoiceList, retrieval, modify, deletion } from "./invoice";
const invoiceController = express.Router();

// /invoice/upload
function validateUploadRequest(req: Request, res: Response, next: NextFunction) {
	const token = req.header("token") as string;
	const { invoicee, einvoice } = req.body;
	if (invoicee === undefined || einvoice === undefined) {
		return res.status(400).json({ message: "Error: Malformed response." });
	}
	if (!validateToken(token)) {
		return res.status(401).json({ message: "Invalid token" });
	}
	next();
}

invoiceController.post("/upload", validateUploadRequest, async (req: Request, res: Response) => {
	const token = req.header("token") as string;
	const { invoicee, einvoice } = req.body;
	const reply = await insert(token, invoicee, einvoice);
	res.status(reply.code).json({ message: reply.message });
});

// /invoice/retrieval
function validateRetrievalRequest(req: Request, res: Response, next: NextFunction) {
	const token = req.header("token") as string;
	const { invoiceId } = req.query;
	if (!validateToken(token)) {
		return res.status(401).json({ message: "Invalid token" });
	}
	if (invoiceId === undefined) {
		return res.status(400).json({ message: "Error: Malformed response." });
	}
	next();
}

invoiceController.get("/retrieval", validateRetrievalRequest, async (req: Request, res: Response) => {
	const token = req.header("token") as string;
	const { invoiceId } = req.query;
	const reply = await retrieval(token as string, invoiceId as string);
	if (reply.message === undefined) {
		res.status(reply.code).json({ einvoice: reply.einvoice, invoicer: reply.invoicer, invoicee: reply.invoicee });
	} else {
		res.status(reply.code).json({ message: reply.message });
	}
});

// /invoice/modify
function validateModifyRequest(req: Request, res: Response, next: NextFunction) {
	const token = req.header("token") as string;
	const { invoiceId, einvoice, invoicee } = req.body;
	if (!validateToken(token)) {
		return res.status(401).json({ message: "Invalid token" });
	}
	if (invoiceId === undefined || einvoice === undefined || invoicee === undefined) {
		return res.status(400).json({ message: "Error: Malformed response." });
	}
	next();
}

invoiceController.put("/modify", validateModifyRequest, async (req: Request, res: Response) => {
	const token = req.header("token") as string;
	const { invoiceId, einvoice, invoicee } = req.body;
	const reply = await modify(invoiceId, token, einvoice, invoicee);
	res.status(reply.code).json({ message: reply.message });
});

// /invoice/list
function validateListRequest(req: Request, res: Response, next: NextFunction) {
	const token = req.header("token") as string;
	if (!validateToken(token)) {
		return res.status(401).json({ message: "Invalid token" });
	}
	next();
}

invoiceController.get("/list", validateListRequest, async (req: Request, res: Response) => {
	const token = req.header("token") as string;
	const { filter } = req.query;
	const reply = await invoiceList(token as string, filter as string);
	res.status(reply.code).json({ message: reply.message });
});

// function validateDeleteRequest(req: Request, res: Response, next: NextFunction) {
// 	const token = req.header("token") as string;
// 	const { invoiceID } = req.body;
// 	if (invoiceID === undefined) {
// 		return res.status(400).json({ message: "Invalid invoice ID" });
// 	}
// 	if (!validateToken(token)) {
// 		return res.status(401).json({ message: "Invalid token" });
// 	}
// 	next();
// }

invoiceController.delete("/delete", /* validateDeleteRequest, */ async (req: Request, res: Response) => {
	const token = req.header("token") as string;
	const { invoiceId } = req.query;
	const reply = await deletion(token as string, invoiceId as string);
	res.status(reply.code).json({ message: reply.message });
});

export { invoiceController };