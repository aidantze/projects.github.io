/**
 * The user-input elements of an invoice submission during the create / modify stage.
 */
export class InvoiceSubmission {
	private file?: File;
	private subject?: string;
	private invoicees?: string;
	private renderFormat?: string;

	public InvoiceSubmission(file?: File, subject?: string, invoicees?: string, renderFormat?: string) {
		this.file = file;
		this.subject = subject;
		this.invoicees = invoicees;
		this.renderFormat = renderFormat;
	}

	public getFile() {
		return this.file;
	}

	public getSubject() {
		return this.subject;
	}

	public getInvoicees() {
		return this.invoicees;
	}

	public getRenderFormat() {
		return this.renderFormat;
	}

	public setFile(file: File): void {
		this.file = file;
	}

	/**
	 * Gets form data relating to the submission during invoice creation / modification.
	 */
	public updateSubmission(): void {
		this.invoicees = (document.getElementById("formInvoicees") as HTMLInputElement).value;
		this.subject = (document.getElementById("formSubject") as HTMLInputElement).value;
		this.renderFormat = (document.getElementById("formRenderFormat") as HTMLInputElement).value;
	}
}