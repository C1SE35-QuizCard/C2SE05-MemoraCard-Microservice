<div align="center">

  [![Quizcards][quizcards-logo]][website-link]

  <h3>Quizcards - Backend</h3>

  A section for Backend  
  
</div>

## 📜 Overview

This is a section for installation and usage for Backend. For more details, go to Quizcards [details](https://github.com/C1SE35-QuizCard/C1SE35-QC-Home/blob/main/README.md)


## ⚙️ Require system & tools<a id="id-require-tools"></a>

**Systems**
-	<b>Processor</b>: Minimum 2 GHz, quad-core recommended.
-	<b>RAM</b>: Minimum 4 GB (8 GB recommended).
-	<b>Storage</b>: At least 16 GB free space for the Java SDK and libraries.

**Tools**
- ☕<b>Java (>= 19)</b> [Download here][download-java]
- 🐬<b>MySQL Server (>= 8.0)</b> [Download here][download-mysql-server]  
- 📊<b>MySQL Workbench (>= 8.0)</b> [Download here][download-mysql-workbench]
- 🌿<b>MongoDB Community Server (>= 8.0)</b> [Download here][download-mongodb-server]
- 🧭<b>MongoDB Compass (>= 1.2)</b> [Download here][download-mongodb-compass]

## 👉 Note

- This backend only runs on Windows 10 (or latest), cannot run on other operating systems.
- You must install all tools from [⚙️ Require tools](#id-require-tools) before going to the [🛠️ Installation & Usage](#️-installation--usage) for setup and running the application.

## 🛠️ Installation & Usage
**🔧 Installation**

1. Clone the repository:
   ```bash
   git clone https://github.com/C1SE35-QuizCard/C1SE35-QuizCard-BE.git
   ```
2. Navigate to the project directory:
   ```bash
   cd C1SE35-QuizCard-BE
   ```
3. Change branch from main to dev:
   ```bash
   git checkout dev
   ```
4. <a id="4-get-data"></a>Get data (SQL format, you must download this file before running application) in [here][download-data-demo].

## 🚀 Usage
1. Change the configuration:
    - From the root project directory, edit the project configuration at <u>`src/main/resources/application.properties`</u>.
        
    - Update the following properties:

        - <a id="u2-database-name"></a>**Database Name**:
          Change the database name in `spring.datasource.url` with the format:
          ```
          spring.datasource.url=jdbc:mysql://localhost:{your_port_configured}/{your_database_name}?createDatabaseIfNotExist=true          
          ```          
          `your_port_configured`: Port on MySQL Server you configured.
          `your_database_name` : Name of the database you want to create.
          
          Example:
          ```
          spring.datasource.url=jdbc:mysql://localhost:3306/quizcards_demo_01?createDatabaseIfNotExist=true
          ```
        - **Database Username & Password**:
          Update `spring.datasource.username` and `spring.datasource.password` with the credentials from your local MySQL setup. 
          
          Example:
          ```
          spring.datasource.username=root
          spring.datasource.password=123456
          ```
        - **Path to downloads**:
          Change the path from `file.storage.path` to any valid drive location on your computer. Example:
          ```
          file.storage.path=E:/System/Downloads
          ```
        - **Setup mongodb**:
          Add 3 lines in the end of file:
          ```
          spring.data.mongodb.uri=mongodb://localhost:27017/
          spring.data.mongodb.database=quizcard_db
          spring.data.mongodb.auto-index-creation=true
          ```
        - **Change the JDK Version**:
          If you installed a JDK that different from my team (default: `JDK 19`), then:
            + From root path on project you cloned, open `build.gradle` file.
            + Change your real version of Java you installed. Example:
              ```java
              java {
                  toolchain {
                      languageVersion = JavaLanguageVersion.of(19) // replace with your JDK version here (ex: 21, 23, ...)
                  }
                  sourceCompatibility = JavaVersion.VERSION_19 // replace with your JDK version here (ex: VERSION_21, VERSION_23, ...)
                  targetCompatibility = JavaVersion.VERSION_19 // replace with your JDK version here (ex: VERSION_21, VERSION_23, ...)
              }
              ```
3. Add data to database:
    - Open the seeds data from `Dump20241229.sql` (File downloaded from Installation > 4, at [here](#4-get-data)):
    - Update the following line:
        In the lines 19 to 21 on file, change the database name to the database you configured on [here](#u2-database-name).
    - Run this file:
        + If you installed MySQL Workbench: Open this file, then press `Ctrl + Shift + Enter` (Make sure you logon to mysql localhost successfully).
        + If not, open CMD, then enter the following code:
          ```bash
          mysql -u your_username -p
          ```
          + Replace your_username with your MySQL account (usually root).
      
          Example:
          ```bash
          mysql -u root -p
          ```
          
          <i>* The system will ask for a password. Enter the password and press Enter.</i>
                  
          Then, use this command to run SQL:
          ```bash
          source /path/to/your/your_path_downloaded_Dump20241229.sql;
          ```
          + `/path/to/your/your_path_downloaded_Dump20241229.sql`: The path on your computer that you downloaded file `Dump20241229.sql`.

          Example:
          ```bash
          source C:/Users/YourName/Documents/Dump20241229.sql;
          ```
4. Build your source:

    - Open CMD.
    - Navigate to the path of your projects you cloned in CMD (`cd \path\C1SE35-QuizCard-BE`), Example:
        ```
        cd C:\Users\YourName\Documents\Projects\C1SE35-QuizCard-BE
        ```
    - Setup temp environment variable:
      + Enter the following lines:
        ```bash
        set JAVA_HOME=\path\of\your\jdk
        set PATH=%JAVA_HOME%\bin;%PATH%
        ```
        Example:
        ```bash
        set JAVA_HOME=D:\Tools\Java\jdk-19
        set PATH=%JAVA_HOME%\bin;%PATH%
        ```
      + Check java valid:
        ```bash
        java --version
        ```
    - Run the command:
      ```bash
      gradlew clean build
      ```
      to build the project.
      <b></b>
      <i>Note: This command will download missing libraries on the project, in `build.gradle` file. Make sure you have connection to download this.</i>

    - After build, run the file build by:
      ```bash
      java -jar build\libs\demo-0.0.1-SNAPSHOT.jar
      ```
      
## Screenshot of the result

1. After running

![Result-after-running][result-after-running]

2. Test some API:

- API Get all categories set (http://localhost:8080/api/v1/category/list):

![Result-01][result-01]
        
- API Get list cards on a set: (http://localhost:8080/api/v1/set/list/{set_id}): 

![Result-list-cards][result-02]

- API Search set: (http://localhost:8080/api/v1/set/search?query={query_search})

![Result-search][result-03]

<b>...</b>

For more examples, see our documents on [Quizcard documents][qc-document-link].

## 🤝 Contributing
We welcome contributions from the open-source community. Check out our contribution guidelines on [README.md](https://github.com/C1SE35-QuizCard/C1SE35-QC-Home/blob/main/README.md#-contributing) for more information.

## 📧 Contact 

For questions, suggestions, or feedback, feel free to reach out:

- Email:

  thanhannbk0912@gmail.com

  letrungkien6@dtu.edu.vn

  yennhi0402dn@gmail.com

- GitHub:

  [An, Trang Thanh](https://github.com/TrangThanhAn)

  [Kien, Le Trung](https://github.com/conmeobeo121)

  [Nhi, Nguyen Thi Yen](https://github.com/macca0402)

Happy Running! 🕸️🚀

[quizcards-logo]:  https://firebasestorage.googleapis.com/v0/b/fir-1-a1ff1.firebasestorage.app/o/images%2Flogo_quizcards%20(1).png?alt=media&token=9ed332b7-818e-41c5-9835-8dc9c9263848
[website-link]:       #
[qc-document-link]: https://drive.google.com/drive/folders/1eOdRawB0MIgvPRS_9QEH7VNpkEA--atL?usp=sharing
[download-java]: https://download.oracle.com/java/19/archive/jdk-19.0.2_windows-x64_bin.msi
[download-mysql-server]: https://dev.mysql.com/downloads/windows/installer/8.0.html
[download-mysql-workbench]: https://dev.mysql.com/downloads/workbench/
[download-mongodb-server]: https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-8.0.4-signed.msi
[download-mongodb-compass]: https://downloads.mongodb.com/compass/mongodb-compass-1.45.0-win32-x64.exe
[download-data-demo]: https://drive.google.com/file/d/169tVmF2jVxptwlqkzki5edASbRyvWP6a/view?usp=sharing
[result-after-running]: https://firebasestorage.googleapis.com/v0/b/fir-1-a1ff1.firebasestorage.app/o/images%2FScreenshot%202025-01-08%20190852.png?alt=media&token=cea7b3ff-7f03-4962-862e-6dde88874c79
[result-01]: https://firebasestorage.googleapis.com/v0/b/fir-1-a1ff1.firebasestorage.app/o/images%2FScreenshot%202025-01-08%20192706.png?alt=media&token=9c1ca396-c8ae-4ce5-be0b-2ee976894c99
[result-02]: https://firebasestorage.googleapis.com/v0/b/fir-1-a1ff1.firebasestorage.app/o/images%2FScreenshot%202025-01-08%20194620.png?alt=media&token=5b1b1dd0-83cb-4100-a8a9-2ed1e7674146
[result-03]: https://firebasestorage.googleapis.com/v0/b/fir-1-a1ff1.firebasestorage.app/o/images%2FScreenshot%202025-01-08%20194738.png?alt=media&token=2e4d60a7-ab50-4631-b38f-f4e88db95f41
