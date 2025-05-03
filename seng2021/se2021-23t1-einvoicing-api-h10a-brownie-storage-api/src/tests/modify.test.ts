import { clear } from "../helper";
import request from "sync-request";
import config from "../config.json";
import { example1, example2 } from "./testdata";

// If server sets up as localhost
const url = process.env.IP || "localhost";
const port = config.port;

let token1: string;
let token2: string;
let invoiceId: string;

beforeEach(async () => {
	const temp = await clear();
	request(
		"POST",
		`http://${url}:${port}` + "/auth/register",
		{ json: { email: "whosinmy@gmail.com", name: "Michael Soft", password: "amazonapple" } }
	);

	const r1 = request(
		"POST",
		`http://${url}:${port}` + "/auth/login",
		{ json: { email: "whosinmy@gmail.com", password: "amazonapple" } }
	);

	const data1 = JSON.parse(r1.getBody() as string);
	token1 = data1.message;

	request(
		"POST",
		`http://${url}:${port}` + "/auth/register",
		{ json: { email: "thisismy@gmail.com", name: "Sam Sung", password: "intelgoogle" } }
	);

	const r2 = request(
		"POST",
		`http://${url}:${port}` + "/auth/login",
		{ json: { email: "thisismy@gmail.com", password: "intelgoogle" } }
	);

	const data2 = JSON.parse(r2.getBody() as string);
	token2 = data2.message;

	const r3 = request(
		"POST",
		`http://${url}:${port}` + "/invoice/upload",
		{ json: { invoicee: "thisismy@gmail.com", einvoice: "hua way" }, headers: { token: token1 } }
	);

	const data3 = JSON.parse(r3.getBody() as string);
	invoiceId = data3.message;

	return temp;
});

describe("Incorrectly using endpoint /modify", () => {
	describe("Empty inputs", () => {
		test("all empty", () => {
			const res = request(
				"PUT",
				`http://${url}:${port}` + "/invoice/modify",
				{
					json: {
						invoiceId: "",
						einvoice: "",
						invoicee: ""
					},
					headers: { token: "" }
				}
			);
			expect(res.statusCode).toBe(401);
		});
		test("invoiceId missing", () => {
			const res = request(
				"PUT",
				`http://${url}:${port}` + "/invoice/modify",
				{
					json: {
						invoiceId: "",
						einvoice: "lynne icks",
						invoicee: "thisismy@gmail.com"
					},
					headers: { token: token1 }
				}
			);
			expect(res.statusCode).toBe(400);
		});
		test("token missing", () => {
			const res = request(
				"PUT",
				`http://${url}:${port}` + "/invoice/modify",
				{
					json: {
						invoiceId: invoiceId,
						einvoice: "lynne icks",
						invoicee: "thisismy@gmail.com"
					},
					headers: { token: "" }
				}
			);
			expect(res.statusCode).toBe(401);
		});
	});

	describe("Inputs do not match constraints", () => {
		test("invalid einvoice: invoice does not exist", () => {
			const res = request(
				"PUT",
				`http://${url}:${port}` + "/invoice/modify",
				{
					json: {
						invoiceId: "2",
						einvoice: "lynne icks",
						invoicee: "thisismy@gmail.com"
					},
					headers: { token: token1 }
				}
			);
			expect(res.statusCode).toBe(400);
		});
		test("invalid token", () => {
			const res = request(
				"PUT",
				`http://${url}:${port}` + "/invoice/modify",
				{
					json: {
						invoiceId: invoiceId,
						einvoice: "lynne icks",
						invoicee: "thisismy@gmail.com"
					},
					headers: { token: "confluence is bad" }
				}
			);
			expect(res.statusCode).toBe(401);
		});
	});

	test("requester is not the invoicer", () => {
		const res = request(
			"PUT",
			`http://${url}:${port}` + "/invoice/modify",
			{
				json: {
					invoiceId: invoiceId,
					einvoice: "lynne icks",
					invoicee: "thisismy@gmail.com"
				},
				headers: { token: token2 }
			}
		);
		expect(res.statusCode).toBe(403);
	});
});

describe("Correctly using endpoint /modify", () => {
	test("modify an invoice with new content", () => {
		const res = request(
			"PUT",
			`http://${url}:${port}` + "/invoice/modify",
			{
				json: {
					invoiceId: invoiceId,
					einvoice: "lynne icks",
					invoicee: "thisismy@gmail.com"
				},
				headers: { token: token1 }
			}
		);
		expect(res.statusCode).toBe(200);
	});

	test("modify an invoice but same content", () => {
		const res = request(
			"PUT",
			`http://${url}:${port}` + "/invoice/modify",
			{
				json: {
					invoiceId: invoiceId,
					einvoice: "hua way",
					invoicee: "thisismy@gmail.com"
				},
				headers: { token: token1 }
			}
		);
		expect(res.statusCode).toBe(200);
	});

	test("modify an invoice but no content", () => {
		const res = request(
			"PUT",
			`http://${url}:${port}` + "/invoice/modify",
			{
				json: {
					invoiceId: invoiceId,
					einvoice: "",
					invoicee: "thisismy@gmail.com"
				},
				headers: { token: token1 }
			}
		);
		expect(res.statusCode).toBe(200);
	});

	test("Checks that invoice put in is the same as invoice out", () => {
		const id1 = request(
			"POST",
			`http://${url}:${port}` + "/invoice/upload",
			{ json: { invoicee: "thisismy@gmail.com", einvoice: example1 }, headers: { token: token1 } }
		);

		const data4 = JSON.parse(id1.getBody() as string);
		const invoice2 = data4.message;

		const res1 = request(
			"PUT",
			`http://${url}:${port}` + "/invoice/modify",
			{
				json: {
					invoiceId: invoice2,
					einvoice: example2,
					invoicee: "thisismy@gmail.com"
				},
				headers: { token: token1 }
			}
		);
		expect(res1.statusCode).toBe(200);

		const res2 = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: invoice2 }, headers: { token: token2 } }
		);

		expect(res2.statusCode).toBe(200);

		const data = JSON.parse(res2.getBody() as string);
		expect(data.einvoice).toStrictEqual(example2);
	});
});
