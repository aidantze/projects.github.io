export default async function makeModifyRequest(invoiceId: string, einvoice: string, invoicee: string, subject: string) {
	let token = "";
	if (typeof window !== "undefined") {
		token = localStorage.getItem("token") || "";
	}

	const invoiceIdsList = "http://h10a-brownie-dev.ap-southeast-2.elasticbeanstalk.com/invoice/modify";
	const url = invoiceIdsList;
	/* const result = */ await fetch(url, {
		method: "PUT",
		headers: {
			token: token,
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			invoiceId: invoiceId as string,
			einvoice: einvoice,
			invoicee: invoicee,
			subject: subject
		})
	});
	// console.log(await result.json());
}
