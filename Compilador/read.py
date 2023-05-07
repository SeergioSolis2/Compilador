# Abrir archivo
with open("read.py", "r") as archivo:
    contenido = archivo.read()

# Diccionarios de tokens
palabras_reservadas = {"if": "IF", "else": "ELSE", "for": "FOR", "while": "WHILE", "int": "INT", "float": "FLOAT", "bool": "BOOL", "true": "TRUE", "false": "FALSE", "print": "PRINT", "return": "RETURN"}
simbolos_especiales = {"(": "PAR_IZQ", ")": "PAR_DER", "{": "LLAVE_IZQ", "}": "LLAVE_DER", ";": "PUNTO_COMA", ",": "COMA"}
operadores_aritmeticos = {"+": "SUMA", "-": "RESTA", "*": "MULTIPLICACION", "/": "DIVISION"}
operadores_relacionales = {"==": "IGUALDAD", "!=": "DIFERENTE", "<": "MENOR_QUE", ">": "MAYOR_QUE", "<=": "MENOR_IGUAL_QUE", ">=": "MAYOR_IGUAL_QUE"}
operadores_logicos = {"&&": "AND", "||": "OR", "!": "NOT"}

# Tokenizar contenido
tokens = []
i = 0
while i < len(contenido):
    # Ignorar espacios en blanco
    if contenido[i].isspace():
        i += 1
        continue
    # Identificar comentarios de una línea
    if contenido[i:i+2] == "//":
        i = contenido.index("\n", i)
        continue
    # Identificar comentarios multilinea
    if contenido[i:i+2] == "/*":
        i = contenido.index("*/", i) + 2
        continue
    # Identificar palabras reservadas, identificadores y números
    if contenido[i].isalpha():
        j = i + 1
        while j < len(contenido) and (contenido[j].isalnum() or contenido[j] == "_"):
            j += 1
        token = contenido[i:j]
        if token in palabras_reservadas:
            tokens.append(palabras_reservadas[token])
        else:
            tokens.append("IDENTIFICADOR(" + token + ")")
        i = j
        continue
    elif contenido[i].isdigit():
        j = i + 1
        while j < len(contenido) and contenido[j].isdigit():
            j += 1
        if j < len(contenido) and contenido[j] == ".":
            j += 1
            while j < len(contenido) and contenido[j].isdigit():
                j += 1
            tokens.append("NUMERO_FLOAT(" + contenido[i:j] + ")")
        else:
            tokens.append("NUMERO_ENTERO(" + contenido[i:j] + ")")
        i = j
        continue
    # Identificar símbolos especiales
    if contenido[i] in simbolos_especiales:
        tokens.append(simbolos_especiales[contenido[i]])
        i += 1
        continue
    # Identificar operadores aritméticos y relacionales
    if contenido[i:i+2] in operadores_relacionales:
        tokens.append(operadores_relacionales[contenido[i:i+2]])
        i += 2
        continue
    elif contenido[i] in operadores_aritmeticos:
        tokens.append(operadores_aritmet)