import { invoiceData } from "@/types/generalTypes";

/**
 * Checks the invoice to see if it matches with the search filter.
 *
 * @param searchFilter search parameter being applied
 * @param invoice the invoice data of which the search is being applied to
 * @returns boolean - whether or not it is a search target or not
 */
export function isSearchTarget(searchFilter: string, invoice: invoiceData): boolean {
	// if search matches anything, return true
	if ((invoice.invoiceeEmail != null && (invoice.invoiceeEmail.toLocaleLowerCase()).includes(searchFilter)) ||
		(invoice.invoiceeName != null && (invoice.invoiceeName.toLocaleLowerCase()).includes(searchFilter)) ||
		(invoice.invoicerName != null && (invoice.invoicerName.toLocaleLowerCase()).includes(searchFilter)) ||
		(invoice.invoicerEmail != null && (invoice.invoicerEmail.toLocaleLowerCase()).includes(searchFilter)) ||
		(invoice.invoiceId != null && (invoice.invoiceId.toLocaleLowerCase()).includes(searchFilter)) ||
		(invoice.created != null && (invoice.created.toLocaleLowerCase()).includes(searchFilter)) ||
		(invoice.modified != null && (invoice.modified.toLocaleLowerCase()).includes(searchFilter)) ||
		(invoice.subject != null && (invoice.subject.toLocaleLowerCase()).includes(searchFilter)) ||
		searchFilter === "") {
		return true;
	}
	return false;
}