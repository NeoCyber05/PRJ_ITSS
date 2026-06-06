CREATE TABLE IF NOT EXISTS "public"."request_edit_lock" (
    "request_id"     integer      NOT NULL,
    "owner_username" varchar(100) NOT NULL,
    "owner_role"     varchar(150) NOT NULL,
    "owner_display"  varchar(150) NOT NULL,
    "locked_at"      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "expires_at"     timestamp    NOT NULL,
    CONSTRAINT request_edit_lock_pkey PRIMARY KEY ("request_id"),
    CONSTRAINT request_edit_lock_request_fk
        FOREIGN KEY ("request_id") REFERENCES "public"."request"("id") ON DELETE CASCADE
);
