import { clear } from "../helper";
import request from "sync-request";
import config from "../config.json";

// If server sets up as localhost
const url = process.env.IP || "localhost";
const port = config.port;

beforeEach(async () => {
	return await clear();
});

describe("Correctly using endpoint /auth/logout", () => {
	test(" succesfully loging out", () => {
		request(
			"POST",
			`http://${url}:${port}` + "/auth/register",
			{ json: { email: "iltg@game.com", name: "AzureDiamond", password: "hunter2" } }
		);

		const login = request(
			"POST",
			`http://${url}:${port}` + "/auth/login",
			{ json: { email: "iltg@game.com", password: "hunter2" } }
		);

		const loginData = JSON.parse(login.getBody() as string);

		const res = request(
			"POST",
			`http://${url}:${port}` + "/auth/logout",
			{ headers: { token: loginData.message } }
		);

		expect(res.statusCode).toStrictEqual(200);
	});
});