module nl.inholland.eindopdracht {
    requires javafx.controls;
    requires javafx.fxml;


    opens nl.inholland.eindopdracht to javafx.fxml;
    exports nl.inholland.eindopdracht;
    exports nl.inholland.eindopdracht.Controllers;
    opens nl.inholland.eindopdracht.Controllers to javafx.fxml;
    exports nl.inholland.eindopdracht.Models;
    opens nl.inholland.eindopdracht.Models to javafx.fxml;
    exports nl.inholland.eindopdracht.Data;
    opens nl.inholland.eindopdracht.Data to javafx.fxml;
    exports nl.inholland.eindopdracht.Controllers.Events;
    opens nl.inholland.eindopdracht.Controllers.Events to javafx.fxml;
}