import { Pool } from "pg";

const pool: Pool = new Pool({
	user: process.env.RDS_PASSWORD || "postgres",
	database: process.env.RDS_DB_NAME || "postgres",
	password: process.env.RDS_PASSWORD || "postgres",
	host: process.env.RDS_HOSTNAME || "localhost",
	port: 5432 || process.env.RDS_PORT
});

const userTable = "CREATE TABLE IF NOT EXISTS users (id serial primary key, email text not null unique, name text not null, password text not null)";
const invoiceTable = "CREATE TABLE IF NOT EXISTS eInvoices (id serial primary key, invoicer integer not null, invoicee integer not null, invoice text not null, created date, modified date, foreign key (invoicer) references users(id), foreign key (invoicee) references users(id))";

pool.query(userTable, (err, res) => {
	if (err) throw err;
});

pool.query(invoiceTable, (err, res) => {
	if (err) throw err;
});

export { pool };