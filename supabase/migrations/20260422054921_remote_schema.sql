


SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


COMMENT ON SCHEMA "public" IS 'standard public schema';



CREATE EXTENSION IF NOT EXISTS "pg_graphql" WITH SCHEMA "graphql";






CREATE EXTENSION IF NOT EXISTS "pg_stat_statements" WITH SCHEMA "extensions";






CREATE EXTENSION IF NOT EXISTS "pgcrypto" WITH SCHEMA "extensions";






CREATE EXTENSION IF NOT EXISTS "supabase_vault" WITH SCHEMA "vault";






CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA "extensions";






CREATE OR REPLACE FUNCTION "public"."rls_auto_enable"() RETURNS "event_trigger"
    LANGUAGE "plpgsql" SECURITY DEFINER
    SET "search_path" TO 'pg_catalog'
    AS $$
DECLARE
  cmd record;
BEGIN
  FOR cmd IN
    SELECT *
    FROM pg_event_trigger_ddl_commands()
    WHERE command_tag IN ('CREATE TABLE', 'CREATE TABLE AS', 'SELECT INTO')
      AND object_type IN ('table','partitioned table')
  LOOP
     IF cmd.schema_name IS NOT NULL AND cmd.schema_name IN ('public') AND cmd.schema_name NOT IN ('pg_catalog','information_schema') AND cmd.schema_name NOT LIKE 'pg_toast%' AND cmd.schema_name NOT LIKE 'pg_temp%' THEN
      BEGIN
        EXECUTE format('alter table if exists %s enable row level security', cmd.object_identity);
        RAISE LOG 'rls_auto_enable: enabled RLS on %', cmd.object_identity;
      EXCEPTION
        WHEN OTHERS THEN
          RAISE LOG 'rls_auto_enable: failed to enable RLS on %', cmd.object_identity;
      END;
     ELSE
        RAISE LOG 'rls_auto_enable: skip % (either system schema or not in enforced list: %.)', cmd.object_identity, cmd.schema_name;
     END IF;
  END LOOP;
END;
$$;


ALTER FUNCTION "public"."rls_auto_enable"() OWNER TO "postgres";

SET default_tablespace = '';

SET default_table_access_method = "heap";


CREATE TABLE IF NOT EXISTS "public"."order" (
    "id" integer NOT NULL,
    "request_id" integer NOT NULL,
    "site_id" integer NOT NULL,
    "created_at" timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    "status" character varying(50)
);


ALTER TABLE "public"."order" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."Order_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."Order_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."Order_id_seq" OWNED BY "public"."order"."id";



CREATE TABLE IF NOT EXISTS "public"."account" (
    "id" integer NOT NULL,
    "username" character varying(50) NOT NULL,
    "password" character varying(255) NOT NULL,
    "full_name" character varying(100),
    "status" character varying(50),
    "role_id" integer NOT NULL,
    "site_id" integer
);


ALTER TABLE "public"."account" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."account_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."account_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."account_id_seq" OWNED BY "public"."account"."id";



CREATE TABLE IF NOT EXISTS "public"."merchandise" (
    "id" integer NOT NULL,
    "code" character varying(50) NOT NULL,
    "name" character varying(100) NOT NULL,
    "unit" character varying(50)
);


ALTER TABLE "public"."merchandise" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."merchandise_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."merchandise_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."merchandise_id_seq" OWNED BY "public"."merchandise"."id";



CREATE TABLE IF NOT EXISTS "public"."order_merchandise" (
    "order_id" integer NOT NULL,
    "merchandise_id" integer NOT NULL,
    "quantity" numeric(10,2) NOT NULL,
    "delivery_method" character varying(50)
);


ALTER TABLE "public"."order_merchandise" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."request" (
    "id" integer NOT NULL,
    "created_at" timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    "status" character varying(50)
);


ALTER TABLE "public"."request" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."request_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."request_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."request_id_seq" OWNED BY "public"."request"."id";



CREATE TABLE IF NOT EXISTS "public"."request_merchandise" (
    "request_id" integer NOT NULL,
    "merchandise_id" integer NOT NULL,
    "quantity_ordered" numeric(10,2) NOT NULL,
    "desired_delivery_date" "date" NOT NULL
);


ALTER TABLE "public"."request_merchandise" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."role" (
    "id" integer NOT NULL,
    "name" character varying(50) NOT NULL
);


ALTER TABLE "public"."role" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."role_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."role_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."role_id_seq" OWNED BY "public"."role"."id";



CREATE TABLE IF NOT EXISTS "public"."site" (
    "id" integer NOT NULL,
    "site_code" character varying(50) NOT NULL,
    "name" character varying(100) NOT NULL,
    "description" "text",
    "ship_delivery_days" integer,
    "air_delivery_days" integer
);


ALTER TABLE "public"."site" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."site_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."site_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."site_id_seq" OWNED BY "public"."site"."id";



CREATE TABLE IF NOT EXISTS "public"."site_inventory" (
    "site_id" integer NOT NULL,
    "merchandise_id" integer NOT NULL,
    "stock_quantity" integer DEFAULT 0
);


ALTER TABLE "public"."site_inventory" OWNER TO "postgres";


ALTER TABLE ONLY "public"."account" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."account_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."merchandise" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."merchandise_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."order" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."Order_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."request" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."request_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."role" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."role_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."site" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."site_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."order"
    ADD CONSTRAINT "Order_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."account"
    ADD CONSTRAINT "account_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."account"
    ADD CONSTRAINT "account_username_key" UNIQUE ("username");



ALTER TABLE ONLY "public"."merchandise"
    ADD CONSTRAINT "merchandise_code_key" UNIQUE ("code");



ALTER TABLE ONLY "public"."merchandise"
    ADD CONSTRAINT "merchandise_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."order_merchandise"
    ADD CONSTRAINT "order_merchandise_pkey" PRIMARY KEY ("order_id", "merchandise_id");



ALTER TABLE ONLY "public"."request_merchandise"
    ADD CONSTRAINT "request_merchandise_pkey" PRIMARY KEY ("request_id", "merchandise_id");



ALTER TABLE ONLY "public"."request"
    ADD CONSTRAINT "request_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."role"
    ADD CONSTRAINT "role_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."site_inventory"
    ADD CONSTRAINT "site_merchandise_pkey" PRIMARY KEY ("site_id", "merchandise_id");



ALTER TABLE ONLY "public"."site"
    ADD CONSTRAINT "site_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."site"
    ADD CONSTRAINT "site_site_code_key" UNIQUE ("site_code");



ALTER TABLE ONLY "public"."order"
    ADD CONSTRAINT "Order_request_id_fkey" FOREIGN KEY ("request_id") REFERENCES "public"."request"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."order"
    ADD CONSTRAINT "Order_site_id_fkey" FOREIGN KEY ("site_id") REFERENCES "public"."site"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."account"
    ADD CONSTRAINT "account_role_id_fkey" FOREIGN KEY ("role_id") REFERENCES "public"."role"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."account"
    ADD CONSTRAINT "account_site_id_fkey" FOREIGN KEY ("site_id") REFERENCES "public"."site"("id") ON DELETE SET NULL;



ALTER TABLE ONLY "public"."order_merchandise"
    ADD CONSTRAINT "order_merchandise_merchandise_id_fkey" FOREIGN KEY ("merchandise_id") REFERENCES "public"."merchandise"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."order_merchandise"
    ADD CONSTRAINT "order_merchandise_order_id_fkey" FOREIGN KEY ("order_id") REFERENCES "public"."order"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."request_merchandise"
    ADD CONSTRAINT "request_merchandise_merchandise_id_fkey" FOREIGN KEY ("merchandise_id") REFERENCES "public"."merchandise"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."request_merchandise"
    ADD CONSTRAINT "request_merchandise_request_id_fkey" FOREIGN KEY ("request_id") REFERENCES "public"."request"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."site_inventory"
    ADD CONSTRAINT "site_merchandise_merchandise_id_fkey" FOREIGN KEY ("merchandise_id") REFERENCES "public"."merchandise"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."site_inventory"
    ADD CONSTRAINT "site_merchandise_site_id_fkey" FOREIGN KEY ("site_id") REFERENCES "public"."site"("id") ON DELETE CASCADE;



ALTER TABLE "public"."account" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."merchandise" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."order" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."order_merchandise" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."request" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."request_merchandise" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."role" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."site" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."site_inventory" ENABLE ROW LEVEL SECURITY;




ALTER PUBLICATION "supabase_realtime" OWNER TO "postgres";


GRANT USAGE ON SCHEMA "public" TO "postgres";
GRANT USAGE ON SCHEMA "public" TO "anon";
GRANT USAGE ON SCHEMA "public" TO "authenticated";
GRANT USAGE ON SCHEMA "public" TO "service_role";

























































































































































GRANT ALL ON FUNCTION "public"."rls_auto_enable"() TO "anon";
GRANT ALL ON FUNCTION "public"."rls_auto_enable"() TO "authenticated";
GRANT ALL ON FUNCTION "public"."rls_auto_enable"() TO "service_role";


















GRANT ALL ON TABLE "public"."order" TO "anon";
GRANT ALL ON TABLE "public"."order" TO "authenticated";
GRANT ALL ON TABLE "public"."order" TO "service_role";



GRANT ALL ON SEQUENCE "public"."Order_id_seq" TO "anon";
GRANT ALL ON SEQUENCE "public"."Order_id_seq" TO "authenticated";
GRANT ALL ON SEQUENCE "public"."Order_id_seq" TO "service_role";



GRANT ALL ON TABLE "public"."account" TO "anon";
GRANT ALL ON TABLE "public"."account" TO "authenticated";
GRANT ALL ON TABLE "public"."account" TO "service_role";



GRANT ALL ON SEQUENCE "public"."account_id_seq" TO "anon";
GRANT ALL ON SEQUENCE "public"."account_id_seq" TO "authenticated";
GRANT ALL ON SEQUENCE "public"."account_id_seq" TO "service_role";



GRANT ALL ON TABLE "public"."merchandise" TO "anon";
GRANT ALL ON TABLE "public"."merchandise" TO "authenticated";
GRANT ALL ON TABLE "public"."merchandise" TO "service_role";



GRANT ALL ON SEQUENCE "public"."merchandise_id_seq" TO "anon";
GRANT ALL ON SEQUENCE "public"."merchandise_id_seq" TO "authenticated";
GRANT ALL ON SEQUENCE "public"."merchandise_id_seq" TO "service_role";



GRANT ALL ON TABLE "public"."order_merchandise" TO "anon";
GRANT ALL ON TABLE "public"."order_merchandise" TO "authenticated";
GRANT ALL ON TABLE "public"."order_merchandise" TO "service_role";



GRANT ALL ON TABLE "public"."request" TO "anon";
GRANT ALL ON TABLE "public"."request" TO "authenticated";
GRANT ALL ON TABLE "public"."request" TO "service_role";



GRANT ALL ON SEQUENCE "public"."request_id_seq" TO "anon";
GRANT ALL ON SEQUENCE "public"."request_id_seq" TO "authenticated";
GRANT ALL ON SEQUENCE "public"."request_id_seq" TO "service_role";



GRANT ALL ON TABLE "public"."request_merchandise" TO "anon";
GRANT ALL ON TABLE "public"."request_merchandise" TO "authenticated";
GRANT ALL ON TABLE "public"."request_merchandise" TO "service_role";



GRANT ALL ON TABLE "public"."role" TO "anon";
GRANT ALL ON TABLE "public"."role" TO "authenticated";
GRANT ALL ON TABLE "public"."role" TO "service_role";



GRANT ALL ON SEQUENCE "public"."role_id_seq" TO "anon";
GRANT ALL ON SEQUENCE "public"."role_id_seq" TO "authenticated";
GRANT ALL ON SEQUENCE "public"."role_id_seq" TO "service_role";



GRANT ALL ON TABLE "public"."site" TO "anon";
GRANT ALL ON TABLE "public"."site" TO "authenticated";
GRANT ALL ON TABLE "public"."site" TO "service_role";



GRANT ALL ON SEQUENCE "public"."site_id_seq" TO "anon";
GRANT ALL ON SEQUENCE "public"."site_id_seq" TO "authenticated";
GRANT ALL ON SEQUENCE "public"."site_id_seq" TO "service_role";



GRANT ALL ON TABLE "public"."site_inventory" TO "anon";
GRANT ALL ON TABLE "public"."site_inventory" TO "authenticated";
GRANT ALL ON TABLE "public"."site_inventory" TO "service_role";









ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "service_role";



































drop extension if exists "pg_net";


