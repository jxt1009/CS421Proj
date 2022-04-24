import random as r;
def generate_data():
    with open("db_gen.txt","w") as file:
        file.write("create table big_test(attr1 Integer primarykey, attr2 Double);\n")
        for i in range(0,10000):
            insert_string = "insert into big_test values("
            insert_string += str(i) + ", "
            insert_string += str(r.randint(0,5000)/500.0) + ");\n"
            file.write(insert_string)

generate_data()

