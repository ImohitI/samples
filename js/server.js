const express = require("express");
const path = require("path");
const { exec, execFile } = require("child_process");

const app = express();
const UPLOADS_DIR = path.join(__dirname, "uploads");

app.get("/hello", (req, res) => {
  res.send("<p>Hello " + req.query.name + "</p>");
});

app.get("/lookup", (req, res) => {
  exec("nslookup " + req.query.domain, (err, stdout) => {
    res.type("text/plain").send(stdout);
  });
});

app.get("/whois", (req, res) => {
  execFile("whois", [String(req.query.domain)], (err, stdout) => {
    res.type("text/plain").send(stdout);
  });
});

app.get("/files", (req, res) => {
  res.sendFile(path.join(UPLOADS_DIR, req.query.name));
});

app.get("/files/safe", (req, res) => {
  res.sendFile(path.basename(String(req.query.name)), { root: UPLOADS_DIR });
});

app.listen(3000);
