import os
from shutil import copyfile

location = os.path.join(os.path.dirname(__file__), "src/main/resources/assets/cyberware/models/item/template.json")
template = open(location, "r")
read = template.read()

def createModel(line):
	text = read.replace("{{name}}", line)
	location2 = os.path.join(os.path.dirname(__file__), "src/main/resources/assets/cyberware/models/item/" + line + ".json")
	index = open(location2, "w")
	index.write(text)
	index.close()

	templateimg = os.path.join(os.path.dirname(__file__), "src/main/resources/assets/cyberware/textures/items/template.png")
	destimg = os.path.join(os.path.dirname(__file__), "src/main/resources/assets/cyberware/textures/items/" + line + ".png")
	copyfile(templateimg, destimg)

with open('todo.txt') as f:
	for line in f:
		index = line.find("cyberware:")
		if index == -1:
			filename = line.strip()
		else:
			filename = line[index + len("cyberware:"):].strip()
		createModel(filename)

template.close()

