package com.lups.mpies.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

@SpringComponent
@UIScope
@Route("")
public class MainView extends VerticalLayout {
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final WebClient webClient = WebClient.create("http://192.168.0.101:8090");
    private String serverResponse;
    private StreamResource downloadableResource;


    public MainView() {
        Html title = new Html("<h1>MPI Execution for Educational Support</h1>");
        add(title);

        TextArea textArea = getArea();

        add(textArea);

        add(new TextInfoBoardView().getInfoBoardLayout());

        HorizontalLayout buttonView = new HorizontalLayout();
        buttonView.setAlignItems(Alignment.CENTER);

        Upload upload = new Upload(buffer);
        upload.setWidth("500px");
        upload.setMaxFiles(3);
        upload.setHeight("200px");


        upload.addSucceededListener(event -> {
            Notification.show("File uploaded successfully: " + event.getFileName()).setPosition(Notification.Position.BOTTOM_END);
        });

        Button uploadButton = new Button("Send Files", event -> {
            try {
                if (!buffer.getFiles().isEmpty()) {
                    postFilesRequest(buffer);

                    Notification.show("File sent successfully.").setPosition(Notification.Position.BOTTOM_END);
                    Notification.show("Result ready to download.").setPosition(Notification.Position.BOTTOM_END);
                } else {
                    Notification.show("Nothing to send.").setPosition(Notification.Position.BOTTOM_END);
                }

            } catch (IOException e) {
                Notification.show("Error sending files.").setPosition(Notification.Position.BOTTOM_END);
            }
        });

        Button downloadButton = new Button("Download Result", event -> {
            Anchor downloadLink;
            try {
                if (this.serverResponse != null) {
                    getFileRequest();
                    downloadLink = new Anchor(this.downloadableResource, "result.txt");

                    add(downloadLink);
                } else {
                    Notification.show("Nothing to download.").setPosition(Notification.Position.BOTTOM_END);
                }

            } catch (IOException e) {
                Notification.show("Error downloading result.").setPosition(Notification.Position.BOTTOM_END);

            }
        });

        buttonView.add(uploadButton, downloadButton);

        add(upload, buttonView);
    }

    private static TextArea getArea() {
        TextArea textArea = new TextArea("Instructions");
        textArea.setHeight("500px");
        textArea.setWidth("1234px");
        textArea.setValue("""
                This is an MPI code execution tool with an educational purpose. The provision of computational resources is a challenge that can affect the understanding of parallel programming concepts. With this in mind, this application aims to address this problem by providing computer science students with virtual machines that parallelize the execution of MPI programs.
                To ensure the correct execution of programs, some precautions are necessary when uploading the code. Below is a functional example for your first test of the application: copy and paste the following code into files named 'Makefile' and 'mpi_hello.c', upload these files, and evaluate the result.

                -> Information regarding the files:
                Be careful when constructing the Makefile. It needs to be correctly formatted to function properly. All compilation and execution commands must be in the 'run' rule.

                In the 'run' rule, you can choose the number of processes to be executed and which hosts will run these processes. The host options are worker0, worker1, and worker2. Each host is a virtual machine at your disposal.
                Examples of choices:
                'mpirun -np 4 -hosts worker1 ./$(PROGRAM)
                'mpirun -np 3 -hosts worker1,worker2,worker3 ./$(PROGRAM)'
                'mpirun -np 5 -hosts worker1,worker2 ./$(PROGRAM)'

                Remember not to change the name of the result.txt file.

                Note 1: To avoid a very high execution load, it is recommended that you test your code before submitting it to the application.
                Note 2: If the Makefile example code is copied and pasted into a file, use tabs instead of spaces.
                """);
        return textArea;
    }

    private void postFilesRequest(MultiFileMemoryBuffer buffer) throws IOException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        Set<String> fileNames = buffer.getFiles();

        for (String fileName : fileNames) {
            ByteArrayResource data = new ByteArrayResource(buffer.getInputStream(fileName).readAllBytes());
            builder.part("file", data)
                    .filename(fileName)
                    .contentType(MediaType.MULTIPART_FORM_DATA);
        }

        Mono<String> monoResponse = webClient.post()
                .uri("/api/files/publish")
                .header("Content-Type", "application/json")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class);

        this.serverResponse = monoResponse.block();
    }

    private void getFileRequest() throws IOException {
        Resource resource;

        Mono<ResponseEntity<Resource>> monoResponse = webClient.get()
                .uri("/api/files/downloadResults/" + this.serverResponse)
                .retrieve()
                .toEntity(Resource.class);

        resource = Objects.requireNonNull(monoResponse.block()).getBody();
        if (resource != null) {
            byte[] fileBytesData = resource.getInputStream().readAllBytes();
            this.downloadableResource = new StreamResource("result.txt", () -> new ByteArrayInputStream(fileBytesData));
        }
    }
}