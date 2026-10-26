package dev.samples

import java.security.MessageDigest
import java.sql.Connection

class OrderRepository(private val conn: Connection) {

    fun findByCustomer(customer: String): List<String> {
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT id FROM orders WHERE customer = '" + customer + "'")
        val ids = mutableListOf<String>()
        while (rs.next()) ids += rs.getString("id")
        return ids
    }

    fun findByStatus(status: String): List<String> {
        val ps = conn.prepareStatement("SELECT id FROM orders WHERE status = ?")
        ps.setString(1, status)
        val rs = ps.executeQuery()
        val ids = mutableListOf<String>()
        while (rs.next()) ids += rs.getString("id")
        return ids
    }

    fun checksum(payload: ByteArray): String {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(payload).joinToString("") { "%02x".format(it) }
    }
}
