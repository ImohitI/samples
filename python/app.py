import os
import sqlite3
import subprocess

from flask import Flask, request, send_file

app = Flask(__name__)
DB_PATH = "users.db"
REPORTS_DIR = "/srv/reports"


def get_db():
    return sqlite3.connect(DB_PATH)


@app.route("/users")
def find_user():
    name = request.args.get("name", "")
    query = f"SELECT id, name, email FROM users WHERE name = '{name}'"
    rows = get_db().execute(query).fetchall()
    return {"users": rows}


@app.route("/users/by-email")
def find_user_by_email():
    email = request.args.get("email", "")
    rows = get_db().execute("SELECT id, name FROM users WHERE email = ?", (email,)).fetchall()
    return {"users": rows}


@app.route("/ping")
def ping():
    host = request.args.get("host", "localhost")
    output = subprocess.check_output("ping -c 1 " + host, shell=True)
    return output


@app.route("/disk")
def disk_usage():
    path = request.args.get("path", "/")
    output = subprocess.run(["du", "-sh", "--", path], capture_output=True, text=True, check=False)
    return output.stdout


@app.route("/reports")
def download_report():
    filename = request.args.get("file", "")
    return send_file(os.path.join(REPORTS_DIR, filename))


@app.route("/reports/safe")
def download_report_safe():
    filename = os.path.basename(request.args.get("file", ""))
    full_path = os.path.realpath(os.path.join(REPORTS_DIR, filename))
    if not full_path.startswith(REPORTS_DIR + os.sep):
        return "forbidden", 403
    return send_file(full_path)
