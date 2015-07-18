JAVAC_FLAGS=-encoding Shift_JIS -classpath $(CLASSPATH)
JAVA_FLAGS=-classpath $(CLASSPATH)
GSON=gson-2.3.1.jar
CLASSPATH=build

all: Bombman.jar

Bombman.jar: build/com/google/gson/Gson.class build/BombmanServer.class build/MANIFEST.MF
	cd build && jar cfm ../$@ MANIFEST.MF .
	chmod +x Bombman.jar

build/com/google/gson/Gson.class: $(GSON)
	unzip -d build -q $(GSON)
	touch $@ # アーカイブから復元された日付を更新する

build/MANIFEST.MF: MANIFEST.MF
	cp $< build

build/BombmanServer.class: BombmanServer.java $(GSON) build/GameState.class
	javac $(JAVAC_FLAGS) $< -d build
build/Block.class: Block.java $(GSON)
	javac $(JAVAC_FLAGS) $< -d build
build/GameState.class: GameState.java $(GSON) build/Bomb.class build/Item.class build/Block.class build/Player.class build/MapData.class
	javac $(JAVAC_FLAGS) $< -d build
build/Bomb.class: Bomb.java $(GSON)
	javac $(JAVAC_FLAGS) $< -d build
build/Item.class: Item.java $(GSON)
	javac $(JAVAC_FLAGS) $< -d build
build/MapData.class: MapData.java $(GSON)
	javac $(JAVAC_FLAGS) $< -d build
build/Player.class: Player.java $(GSON)
	javac $(JAVAC_FLAGS) $< -d build
build/Position.class: Position.java $(GSON)
	javac $(JAVAC_FLAGS) $< -d build

clean:
	rm -rf build/*
