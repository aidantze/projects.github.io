# COMP3311 23T1 Assignment 2 ... Python helper functions
# add any functions to share between Python scripts
# Note: you must submit this file even if you add nothing to it

import re

def clean(s: str) -> str:
    """
    Clean user input
    remove leading and trailing whitespace
    convert to title case (first letter of each word is uppercase, the rest are lowercase)
    squish multiple whitespace characters into a single space
    """
    return re.sub(r'\s+', ' ', s.strip().title())


def myPokemonTupleToStr(nameTuple):
	"""
	Takes in a list of tuples
	Returns a new list containing only first (0th) index of each tuple.
	"""
	strList = []
	for i in nameTuple:
		strList.append(i[0])
	return strList

def pokemonEvolutionsPrintRequirements(requireList):
	"""
	Takes in a list of tuples containing specific information about evolution requirements
	Formats and prints requirements, along with associated logic.
	Used to manage number of tab indents, inverted requirements and AND/OR keywords.
	Returns nothing
	"""
	# variables used to determine number of indents for print output
	orExists = False
	andExists = False
	tabs = ""
	andTabs = ""
	andOrRequireList = []

	for j in range(len(requireList)):
		evolveId, _, inv, requireName = requireList[j]
		if inv:
			requireName = "NOT " + requireName

		if j + 1 >= len(requireList):
			# end the loop
			andOrRequireList.append({
				"requireName": requireName,
				"logic": ""
			})
			break
		evolveId2 = requireList[j + 1][0]

		if evolveId == evolveId2:
			andOrRequireList.append({
				"requireName": requireName,
				"logic": "AND"
			})
			andExists = True
		else:
			andOrRequireList.append({
				"requireName": requireName,
				"logic": "OR"
			})
			orExists = True

	if orExists and andExists:
		tabs = "\t\t"
		andTabs = "\t"
	elif orExists or andExists:
		tabs = "\t"
				
	for j in andOrRequireList:
		if j["logic"] == "AND":
			print(f"\t\t{tabs}{j['requireName']}\n\t\t{andTabs}AND")
		elif j["logic"] == "OR":
			print(f"\t\t{tabs}{j['requireName']}\n\t\tOR")
		else:
			print(f"\t\t{tabs}{j['requireName']}")
	print()
