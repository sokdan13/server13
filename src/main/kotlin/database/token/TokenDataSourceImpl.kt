package com.example.database.token

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class TokenDataSourceImpl: TokenDataSource {
    override suspend fun insertToken(token: Token): Boolean {
        try{
            newSuspendedTransaction {
                TokenTable.insert {
                    it[userId] = token.userId
                    it[refreshToken] = token.refreshToken
                    it[issuedAt] = token.issuedAt
                    it[expiresAt] = token.expiresAt
                    it[revoked] = token.revoked
                }
            }
            return true
        } catch (_: Exception) {
            return false
        }
    }

    override suspend fun findToken(refreshToken: String): Token? {
        return newSuspendedTransaction {
            TokenTable
                .selectAll()
                .where(TokenTable.refreshToken eq refreshToken)
                .map {
                    Token(
                        it[TokenTable.userId],
                        it[TokenTable.refreshToken],
                        it[TokenTable.issuedAt],
                        it[TokenTable.expiresAt],
                        it[TokenTable.revoked]
                    )
                }
                .firstOrNull()
        }
    }



    override suspend fun updateToken(token: Token): Int {
        return newSuspendedTransaction {
            TokenTable.update({
                TokenTable.userId eq token.userId
            }){
                it[TokenTable.refreshToken] = token.refreshToken
                it[issuedAt] = token.issuedAt
                it[TokenTable.expiresAt] = token.expiresAt
            }
        }
    }
}