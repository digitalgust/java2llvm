TARGET=$(shell basename `pwd`)
LLS=$(wildcard *.ll)
CS=$(wildcard *.c)
ASMS=$(LLS:%.ll=%.s)
ASMS2=$(CS:%.c=%.s)

all: $(TARGET)

$(TARGET): $(ASMS) $(ASMS2)
	clang -v -o $(TARGET) $(ASMS) $(ASMS2)
#	strip $(TARGET)

%.ll: %.c
#	clang -S -emit-llvm -o $@ $<
	clang -S -emit-llvm $<

%.bc: %.ll
	llvm-as $<


%.s: %.bc
	llc $<


clean:
	rm -f ./*.s
	rm -f $(TARGET)
	rm -f ./*.ll
	rm -f ./*.bc
	rm -f ./*.gch
