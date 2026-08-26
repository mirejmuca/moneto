package com.moneto.api.model;

public enum AlertType {
    DAILY_SPENDING,      // shpenzime ditore mbi X
    CATEGORY_SPENDING,   // shpenzime për një kategori këtë muaj mbi X
    MONTHLY_SPENDING,    // shpenzime totale të muajit mbi X
    LARGE_TRANSACTION,   // një transaksion i vetëm mbi X
    NEGATIVE_BALANCE     // shpenzimet e muajit kalojnë të ardhurat
}