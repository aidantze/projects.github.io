import { invoiceData } from "@/types/generalTypes";

export class sendInvoice {
	static apiUrl = "http://h13a-applepie-v42-env.eba-smxshheg.ap-southeast-2.elasticbeanstalk.com";

	static async sendSMSInvoice(invoice: invoiceData, target: string, message = "Invoice for you - sent by PieConnect.") {
		const res = await fetch((this.apiUrl + "/sms"), {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({
				xmlString: invoice.invoiceText,
				recipients: [
					target,
				],
				subject: invoice.subject || "You have an incoming invoice!",
				message: message,
				format: "HTML",
			})
		});

		const reply = await res.json();
		alert("SMS sent!");
		return (reply.message)?.toString();
	}

	static async sendEmailInvoice(invoice: invoiceData, message = "Invoice for you - sent by PieConnect.") {
		let res;
		try {
			res = await fetch((this.apiUrl + "/email"), {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					xmlString: invoice.invoiceText,
					recipients: [
						invoice.invoiceeEmail,
					],
					subject: invoice.subject || "You have an incoming invoice!",
					message: message,
					format: "HTML",
				})
			});
			const reply = await res.json();
			alert("Email sent!");
			return (reply.message)?.toString();
		} catch {
			console.log("Redirect error!");
		}
	}

	static async getDataFromId(invoiceId: string) {
		let token = "";
		if (typeof window !== "undefined") {
			token = localStorage.getItem("token") || "";
		}
		const invoiceIdsList = `http://h10a-brownie-dev.ap-southeast-2.elasticbeanstalk.com/invoices/retrieve?filter=specific&ids[0]=${invoiceId}`;
		const url = invoiceIdsList;
		const result = await fetch(url, {
			method: "GET",
			headers: {
				token: token
			}
		});
		const rText = await result.json();
		const data = rText.invoices[0];
		console.log(data);
		const invoiceData: invoiceData = {
			invoiceId: data.invoiceId,
			invoicerName: data.invoicerName,
			invoicerEmail: data.invoicerEmail,
			invoiceeName: data.invoiceeName,
			invoiceeEmail: data.invoiceeEmail,
			created: data.created,
			modified: data.created,
			invoiceText: data.invoice,
			subject: data.subject
		};
		return invoiceData;
	}
}