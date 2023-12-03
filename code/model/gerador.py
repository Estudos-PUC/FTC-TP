import itertools

# Regras da gramática
rules = {
    "S": ["AED", "F", "SI", "HS"],
    "A": ["Aa", "a", "AB"],
    "B": ["Bb", "b", "BA"],
    "C": ["Cc", "c", "CI"],
    "D": ["Dd", "d", "DC"],
    "E": ["bEc", "bc", "EA"],
    "F": ["aFd", "BC", "FB"],
    "H": ["abbbbc", "HA"],
    "I": ["Id", "IA", "IH"]
}

# Terminais fornecidos
terminals = ["a", "b", "c", "d"]

# Função para gerar combinações de terminais
def generate_combinations(symbol, depth, max_depth):
    if depth > max_depth:
        return []

    combinations = []
    if symbol in terminals:
        return [symbol]
    elif symbol in rules:
        for production in rules[symbol]:
            sub_combinations = [generate_combinations(sub_symbol, depth + 1, max_depth) 
                                for sub_symbol in production]
            product = ["".join(combo) for combo in itertools.product(*sub_combinations)]
            combinations.extend(product)
    return combinations

# Gerar combinações
max_depth = 5   	  # Limitar a profundidade para evitar recursão infinita
start_symbol = "S"  # Símbolo inicial

# Gerar combinações a partir do símbolo inicial
terminal_combinations = generate_combinations(start_symbol, 0, max_depth)

# Filtrar combinações únicas e não vazias
unique_combinations = set(filter(None, terminal_combinations))

# Exibir as combinações
print(len(unique_combinations))
for combo in unique_combinations:
   print(combo)

