import sys
import codecs

path = 'src/main/java/org/example/screen/HomeScreen.java'
try:
    with codecs.open(path, 'r', 'utf-8') as f:
        content = f.read()

    # Modify header
    content = content.replace('header.getChildren().addAll(logoIcon, title, spacer, settingsBtn);',
                              'Button notifBtn = new Button("\\U0001F514");\n' +
                              '        notifBtn.getStyleClass().addAll("sidebar-icon-btn");\n' +
                              '        notifBtn.setTooltip(new Tooltip("Notificaciones"));\n' +
                              '        notifBtn.setOnAction(e -> parent.showNotifications());\n' +
                              '        org.example.provider.FriendRequestProvider.getInstance().getPendingRequests().addListener((javafx.collections.ListChangeListener<org.example.model.FriendRequest>) c -> {\n' +
                              '            if (org.example.provider.FriendRequestProvider.getInstance().getPendingRequests().isEmpty()) {\n' +
                              '                notifBtn.setStyle("");\n' +
                              '            } else {\n' +
                              '                notifBtn.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");\n' +
                              '            }\n' +
                              '        });\n\n' +
                              '        header.getChildren().addAll(logoIcon, title, spacer, notifBtn, settingsBtn);')

    # Modify new chat behavior
    content = content.replace('parent.showAddContact();', 'parent.showContactsList();')

    with codecs.open(path, 'w', 'utf-8') as f:
        f.write(content)
    print('Done editing HomeScreen.java')
except Exception as e:
    print('Error:', e)
