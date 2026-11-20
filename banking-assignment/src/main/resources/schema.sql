PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS customers (
    id TEXT PRIMARY KEY,
    first_name TEXT NOT NULL,
    surname TEXT NOT NULL,
    address TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE COLLATE NOCASE,
    password_hash TEXT NOT NULL,
    employer TEXT NOT NULL DEFAULT '',
    employer_address TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS accounts (
    account_number TEXT PRIMARY KEY,
    customer_id TEXT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    account_type TEXT NOT NULL CHECK (account_type IN ('SAVINGS','INVESTMENT','CHEQUE')),
    branch TEXT NOT NULL,
    date_opened TEXT NOT NULL,
    balance_cents INTEGER NOT NULL DEFAULT 0 CHECK (balance_cents >= 0)
);

CREATE INDEX IF NOT EXISTS idx_accounts_customer ON accounts(customer_id);

CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_number TEXT NOT NULL REFERENCES accounts(account_number) ON DELETE CASCADE,
    kind TEXT NOT NULL CHECK (kind IN ('OPENING','DEPOSIT','WITHDRAWAL','INTEREST')),
    amount_cents INTEGER NOT NULL CHECK (amount_cents >= 0),
    balance_after_cents INTEGER NOT NULL CHECK (balance_after_cents >= 0),
    reference TEXT NOT NULL,
    occurred_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_account ON transactions(account_number, id);

CREATE TABLE IF NOT EXISTS interest_postings (
    account_number TEXT NOT NULL REFERENCES accounts(account_number) ON DELETE CASCADE,
    month_key TEXT NOT NULL,
    PRIMARY KEY (account_number, month_key)
);
