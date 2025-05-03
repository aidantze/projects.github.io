function b64encode(input: string): string {
	return Buffer.from(input, "utf8").toString("base64");
}

function b64decode(input: string): string {
	return Buffer.from(input, "base64").toString("utf8");
}

export { b64encode, b64decode };