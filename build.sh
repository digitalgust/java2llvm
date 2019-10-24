echo need : make clang llvm-as llc java

echo compile java source *.java and convert to llvm ir file *.ll
java -cp ./class2ir/dist/class2ir.jar:./class2ir/lib/asm-7.2.jar:./class2ir/lib/classparser.jar j2ll.Main ./app/java ./app/out/classes ./app/c/

cd ./app/c/
make
cp ./c ../../app.exe
make clean 
cd ../../
rm -rf ./app/out/classes/*
rm -rf ./class2ir/target
echo success
