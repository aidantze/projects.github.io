import { clear } from "../helper";
import request from "sync-request";
import config from "../config.json";
import { example1, example2 } from "./testdata";

// If server sets up as localhost
const url = process.env.IP || "localhost";
const port = config.port;

let token1: string;
let token2: string;
let token3: string;
let invoice1: string;

beforeEach(async () => {
	const temp = await clear();
	request(
		"POST",
		`http://${url}:${port}` + "/auth/register",
		{ json: { email: "Vincent@gmail.com", name: "VinceMarc", password: "passpass" } }
	);

	const r1 = request(
		"POST",
		`http://${url}:${port}` + "/auth/login",
		{ json: { email: "Vincent@gmail.com", password: "passpass" } }
	);

	const data1 = JSON.parse(r1.getBody() as string);
	token1 = data1.message;

	request(
		"POST",
		`http://${url}:${port}` + "/auth/register",
		{ json: { email: "Marcus@gmail.com", name: "MarcVince", password: "wordword" } }
	);

	const r2 = request(
		"POST",
		`http://${url}:${port}` + "/auth/login",
		{ json: { email: "Marcus@gmail.com", password: "wordword" } }
	);

	const data2 = JSON.parse(r2.getBody() as string);
	token2 = data2.message;

	request(
		"POST",
		`http://${url}:${port}` + "/auth/register",
		{ json: { email: "Bato@gmail.com", name: "VinceBato", password: "wordpass" } }
	);

	const r3 = request(
		"POST",
		`http://${url}:${port}` + "/auth/login",
		{ json: { email: "Bato@gmail.com", password: "wordpass" } }
	);

	const data3 = JSON.parse(r3.getBody() as string);
	token3 = data3.message;

	const id1 = request(
		"POST",
		`http://${url}:${port}` + "/invoice/upload",
		{ json: { invoicee: "Marcus@gmail.com", einvoice: "coolinvoice" }, headers: { token: token1 } }
	);

	const data4 = JSON.parse(id1.getBody() as string);
	invoice1 = data4.message;

	return temp;
});

describe("Tests for retrieval that should return error messages", () => {
	test("Give an empty token", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: invoice1 }, headers: { token: "" } }
		);
		expect(res.statusCode).toBe(401);
	});

	test("The token passed is not valid", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: invoice1 }, headers: { token: "BadToken" } }
		);
		expect(res.statusCode).toBe(401);
	});

	test("Give an empty invoiceId", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: "" }, headers: { token: token1 } }
		);
		expect(res.statusCode).toBe(400);
	});

	test("Give invoiceId that does not exist", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: "BadInvoiceId" }, headers: { token: token1 } }
		);
		expect(res.statusCode).toBe(401);
	});

	test("Both fields are empty", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: "" }, headers: { token: "" } }
		);
		expect(res.statusCode).toBe(401);
	});

	test("Both fields are wrong", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: "BadInvoiceId" }, headers: { token: "BadToken" } }
		);
		expect(res.statusCode).toBe(401);
	});

	test("The requester is neither an invoicer or invoicee - no permission", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: invoice1 }, headers: { token: token3 } }
		);
		expect(res.statusCode).toBe(401);
	});
});

describe("Tests for retrieval that should return successfully", () => {
	test("Checks that invoicer can see invoice", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: invoice1 }, headers: { token: token1 } }
		);
		expect(res.statusCode).toBe(200);

		const data = JSON.parse(res.getBody() as string);
		expect(data.einvoice).toStrictEqual(expect.any(String));
		expect(data.invoicer).toStrictEqual(expect.any(Number));
		expect(data.invoicee).toStrictEqual(expect.any(String));
	});

	test("Checks that invoicee can see invoice", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: invoice1 }, headers: { token: token2 } }
		);
		expect(res.statusCode).toBe(200);

		const data = JSON.parse(res.getBody() as string);
		expect(data.einvoice).toStrictEqual(expect.any(String));
		expect(data.invoicer).toStrictEqual(expect.any(Number));
		expect(data.invoicee).toStrictEqual(expect.any(String));
	});

	test("Checks that invoice put in is the same as invoice out", () => {
		const id1 = request(
			"POST",
			`http://${url}:${port}` + "/invoice/upload",
			{ json: { invoicee: "Marcus@gmail.com", einvoice: example1 }, headers: { token: token1 } }
		);

		const data4 = JSON.parse(id1.getBody() as string);
		const invoice2 = data4.message;

		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: invoice2 }, headers: { token: token2 } }
		);
		expect(res.statusCode).toBe(200);
		const data = JSON.parse(res.getBody() as string);
		expect(data.einvoice).toStrictEqual(example1);
	});

	test("Checks that invoice put in is the same as invoice out", () => {
		const id1 = request(
			"POST",
			`http://${url}:${port}` + "/invoice/upload",
			{ json: { invoicee: "Marcus@gmail.com", einvoice: example2 }, headers: { token: token1 } }
		);

		const data4 = JSON.parse(id1.getBody() as string);
		const invoice2 = data4.message;

		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/retrieval",
			{ qs: { invoiceId: invoice2 }, headers: { token: token2 } }
		);
		expect(res.statusCode).toBe(200);
		const data = JSON.parse(res.getBody() as string);
		expect(data.einvoice).toStrictEqual(example2);
	});
});
