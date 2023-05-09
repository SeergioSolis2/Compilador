# -*- coding: utf-8 -*-
import sys
import re

contenido = ""
for i in range(0,len(sys.argv)):
    contenido += str(sys.argv[i])
    contenido+='\n'

# Diccionarios de tokens
palabras_reservadas = {"main": "palabra reservada", "then": "palabra reservada", "if": "palabra reservada", "else": "palabra reservada", "end": "palabra reservada", "do": "palabra reservada", "while": "palabra reservada", "repeat": "palabra reservada", "until": "palabra reservada", "cin": "palabra reservada", "cout": "palabra reservada", "real": "palabra reservada", "int": "palabra reservada", "boolean": "palabra reservada", "true": "palabra reservada", "false": "palabra reservada"}
simbolos_especiales = {"(": "PAR_IZQ", ")": "PAR_DER", "{": "LLAVE_IZQ", "}": "LLAVE_DER", ";": "PUNTO_COMA", ",": "COMA"}
operadores_aritmeticos = {"+": "SUMA", "-": "RESTA", "*": "MULTIPLICACION", "/": "DIVISION", "=": "IGUALACION"}
operadores_relacionales = {"==": "IGUALDAD", "!=": "DIFERENTE", "<>":"DIFERENTE2","<": "MENOR_QUE", ">": "MAYOR_QUE", "<=": "MENOR_IGUAL_QUE", ">=": "MAYOR_IGUAL_QUE"}
operadores_logicos = {"&&": "AND", "||": "OR", "!": "NOT"}
operadores_dobles = {"++": "INCREMENTO", "--": "DECREMENTO"}
# Tokenizar contenido
tokens = []
linea = 1
col = 1

i = 0
while i < len(contenido):
    # Ignorar espacios en blanco
    if contenido[i].isspace():
        if (contenido[i] == "\n"):
            linea +=1
            col = 1
        i += 1
        continue
    # Identificar comentarios de una línea
    if contenido[i:i+2] == "//":
        i = contenido.index("\n", i)
        continue
    # Identificar comentarios multilinea
    if contenido[i:i+2] == "/*":
        aux = contenido.index("*/", i) + 2
        j = i
        while j < aux:
            if contenido[j] == '\n':
                linea+=1
            j+=1
        i = aux
        continue
    # Identificar palabras reservadas, identificadores y números
    if contenido[i].isalpha():
        j = i + 1
        while j < len(contenido) and (contenido[j].isalnum() or contenido[j] == "_"):
            j += 1
            col+=1
        token = contenido[i:j]
        if token in palabras_reservadas:
            tokens.append("[" + token + ", "  + palabras_reservadas[token] +"]")
        else:
            tokens.append("[" + token + ", ídentificador]")
        i = j
        continue
    elif contenido[i].isdigit():
        j = i + 1
        while j < len(contenido) and contenido[j].isdigit():
            j += 1
            col+=1
        if j < len(contenido) and contenido[j] == ".":
            j += 1
            col+=1
            while j < len(contenido) and contenido[j].isdigit():
                j += 1
                col+=1
            tokens.append("[" + contenido[i:j] + ", flotante]")
        else:
            tokens.append("[" + contenido[i:j] + ", entero]")
        i = j
        continue
    # Identificar símbolos especiales
    if contenido[i] in simbolos_especiales:
        tokens.append("[" + contenido[i] + ", simbolo especial]")
        i += 1
        col+=1
        continue
    # Identificar operadores aritméticos y relacionales
    if contenido[i:i+2] in operadores_relacionales:
        tokens.append("[" + operadores_relacionales[contenido[i:i+2]] + ", operador relacional]")
        i += 2
        col+=2
        continue
    elif contenido[i] in operadores_relacionales:
        tokens.append("[" + operadores_relacionales[contenido[i]] + ", operador relacional]")
        i += 1
        continue
    if contenido[i:i+2] in operadores_dobles:
        tokens.append("[" +contenido[i:i+2] + ", operador aritmetico]")
        i += 2
        col+=1
        continue
    elif contenido[i] in operadores_aritmeticos:
        tokens.append("[" + contenido[i] + ", operador aritmetico]")
        i +=1
        col+=1
        continue
    else: 
        tokens.append("error:'" + contenido[i] + "'(linea:" + str(linea) +", columna: " + str(col+1) + ")")
        i+=1
        col+=1
        continue
for item in tokens:
    print(item)
