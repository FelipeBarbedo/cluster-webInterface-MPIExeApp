package com.lups.mpies.views;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;

public class TextInfoBoardView extends HorizontalLayout {

    private final HorizontalLayout infoBoardLayout;

    public TextInfoBoardView() {
        infoBoardLayout = new HorizontalLayout();

        infoBoardLayout.setAlignItems(Alignment.STRETCH);

        TextArea makefileInfoBoard = new TextArea("Makefile");
        TextArea cHelloInfoBoard = new TextArea("mpi_hello.c");

        makefileInfoBoard.setReadOnly(true);
        makefileInfoBoard.setWidth("600px");
        makefileInfoBoard.setHeight("600px");
        makefileInfoBoard.setValue("#number_of_virtual_machines:2\n" +
                                   "PROGRAM = mpi_hello\n" +
                                   "\n" +
                                   "# Your MPI C compiler\n" +
                                   "MPICC = mpicc\n" +
                                   "SRCS = mpi_hello.c\n" +
                                   "\n" +
                                   "HOSTS = --hosts manager,worker0" +
                                   "\n" +
                                   "# Rules. Note: MAKE SURE YOU ARE USING A TAB INSTEAD OF SPACES.\n" +
                                   "all: $(PROGRAM)\n" +
                                   "\n" +
                                   "$(PROGRAM): $(SRCS)\n" +
                                   "    $(MPICC) -o $(PROGRAM) $(SRCS)\n" +
                                   "\n" +
                                   "run: $(PROGRAM)\n" +
                                   "    mpirun -np 4 $(HOSTS) ./$(PROGRAM)\n" +
                                   "clean:\n" +
                                   "    rm -f $(PROGRAM)");

        cHelloInfoBoard.setReadOnly(true);
        cHelloInfoBoard.setWidth("600px");
        cHelloInfoBoard.setHeight("600px");
        cHelloInfoBoard.setValue("#include <mpi.h>\n" +
                                 "#include <stdio.h>\n" +
                                 "#include <string.h>\n" +
                                 "\n" +
                                 "int main(int argc, char *argv[]) {\n" +
                                 "    int rank, size, namelen;\n" +
                                 "    char hostname[MPI_MAX_PROCESSOR_NAME];\n" +
                                 "\n" +
                                 "    MPI_Init(&argc, &argv);\n" +
                                 "    MPI_Comm_rank(MPI_COMM_WORLD, &rank);\n" +
                                 "    MPI_Comm_size(MPI_COMM_WORLD, &size);\n" +
                                 "    MPI_Get_processor_name(hostname, &namelen);\n" +
                                 "\n" +
                                 "    printf(\"Hello, world! I am process %d of %d running on %s\\n\", rank, size, hostname);\n" +
                                 "\n" +
                                 "    MPI_Finalize();\n" +
                                 "    return 0;\n" +
                                 "}");

        infoBoardLayout.add(makefileInfoBoard);
        infoBoardLayout.add(cHelloInfoBoard);
    }

    public HorizontalLayout getInfoBoardLayout() {
        return this.infoBoardLayout;
    }
}
