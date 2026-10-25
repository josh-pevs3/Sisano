/* =====================================================================
   SISANO - Gestión de Citas Médicas
   Script de creación de la base de datos para SQL Server
   Base de datos : SISANO_DB   (la que usa DatabaseConnection.java)
   Ejecutar en   : SQL Server Management Studio (SSMS) o Azure Data Studio
                   con un usuario con permisos de sysadmin (p. ej. sa)
   ADVERTENCIA   : si las tablas ya existen, este script las ELIMINA y
                   las vuelve a crear (se pierden los datos).
   ===================================================================== */

IF DB_ID(N'SISANO_DB') IS NULL
    CREATE DATABASE SISANO_DB;
GO

USE SISANO_DB;
GO

/* ---------- 1. Limpieza (orden inverso a las dependencias) ---------- */
IF OBJECT_ID(N'dbo.Cita', N'U')           IS NOT NULL DROP TABLE dbo.Cita;
IF OBJECT_ID(N'dbo.Disponibilidad', N'U') IS NOT NULL DROP TABLE dbo.Disponibilidad;
IF OBJECT_ID(N'dbo.Paciente', N'U')       IS NOT NULL DROP TABLE dbo.Paciente;
IF OBJECT_ID(N'dbo.Medico', N'U')         IS NOT NULL DROP TABLE dbo.Medico;
IF OBJECT_ID(N'dbo.Sala', N'U')           IS NOT NULL DROP TABLE dbo.Sala;
IF OBJECT_ID(N'dbo.Especialidad', N'U')   IS NOT NULL DROP TABLE dbo.Especialidad;
IF OBJECT_ID(N'dbo.Usuario', N'U')        IS NOT NULL DROP TABLE dbo.Usuario;
GO

/* ---------- 2. Tablas ---------- */

-- Usuarios del sistema (login). Roles: ADMIN / SECRETARIO
-- Nota: el programa compara la contraseña en texto plano contra password_hash.
CREATE TABLE dbo.Usuario (
    id_usuario      INT IDENTITY(1,1) NOT NULL,
    username        NVARCHAR(50)  NOT NULL,
    password_hash   NVARCHAR(255) NOT NULL,
    rol             NVARCHAR(20)  NOT NULL,
    nombre_completo NVARCHAR(100) NOT NULL,
    CONSTRAINT PK_Usuario          PRIMARY KEY (id_usuario),
    CONSTRAINT UQ_Usuario_username UNIQUE (username),
    CONSTRAINT CK_Usuario_rol      CHECK (rol IN (N'ADMIN', N'SECRETARIO'))
);
GO

-- Catálogo de especialidades (llena el combo de la pantalla Médicos)
CREATE TABLE dbo.Especialidad (
    id_especialidad INT IDENTITY(1,1) NOT NULL,
    nombre          NVARCHAR(100) NOT NULL,
    CONSTRAINT PK_Especialidad        PRIMARY KEY (id_especialidad),
    CONSTRAINT UQ_Especialidad_nombre UNIQUE (nombre)
);
GO

-- Médicos (la especialidad se guarda como texto, así lo hace el programa)
CREATE TABLE dbo.Medico (
    id_medico    INT IDENTITY(1,1) NOT NULL,
    codigo       NVARCHAR(20)  NOT NULL,
    nombre       NVARCHAR(100) NOT NULL,
    especialidad NVARCHAR(100) NULL,
    email        NVARCHAR(100) NULL,
    telefono     NVARCHAR(20)  NULL,
    sexo         CHAR(1)       NOT NULL,
    CONSTRAINT PK_Medico        PRIMARY KEY (id_medico),
    CONSTRAINT UQ_Medico_codigo UNIQUE (codigo),
    CONSTRAINT CK_Medico_sexo   CHECK (sexo IN ('M', 'F'))
);
GO

CREATE TABLE dbo.Paciente (
    id_paciente      INT IDENTITY(1,1) NOT NULL,
    dni              NVARCHAR(15)  NOT NULL,
    nombre           NVARCHAR(100) NOT NULL,
    email            NVARCHAR(100) NULL,
    telefono         NVARCHAR(20)  NULL,
    fecha_nacimiento DATE          NULL,
    genero           CHAR(1)       NULL,
    CONSTRAINT PK_Paciente     PRIMARY KEY (id_paciente),
    CONSTRAINT UQ_Paciente_dni UNIQUE (dni),
    CONSTRAINT CK_Paciente_gen CHECK (genero IN ('M', 'F'))
);
GO

-- Salas / consultorios. estado: 1 = habilitada, 0 = en mantenimiento
CREATE TABLE dbo.Sala (
    id_sala INT IDENTITY(1,1) NOT NULL,
    nombre  NVARCHAR(50) NOT NULL,
    estado  BIT          NOT NULL CONSTRAINT DF_Sala_estado DEFAULT 1,
    CONSTRAINT PK_Sala        PRIMARY KEY (id_sala),
    CONSTRAINT UQ_Sala_nombre UNIQUE (nombre)
);
GO

-- Bloques horarios: un médico en una sala, en una fecha y hora.
-- estado: ASIGNADO (recién asignado), LIBRE, OCUPADO / RESERVADO (con cita)
CREATE TABLE dbo.Disponibilidad (
    id_disponibilidad INT IDENTITY(1,1) NOT NULL,
    id_medico         INT         NOT NULL,
    id_sala           INT         NOT NULL,
    fecha             DATE        NOT NULL,
    hora_inicio       TIME(0)     NOT NULL,
    hora_fin          TIME(0)     NOT NULL,
    estado            NVARCHAR(20) NOT NULL CONSTRAINT DF_Disp_estado DEFAULT N'ASIGNADO',
    CONSTRAINT PK_Disponibilidad PRIMARY KEY (id_disponibilidad),
    CONSTRAINT FK_Disp_Medico FOREIGN KEY (id_medico) REFERENCES dbo.Medico (id_medico),
    CONSTRAINT FK_Disp_Sala   FOREIGN KEY (id_sala)   REFERENCES dbo.Sala   (id_sala),
    CONSTRAINT CK_Disp_horas  CHECK (hora_fin > hora_inicio),
    CONSTRAINT CK_Disp_estado CHECK (estado IN (N'ASIGNADO', N'LIBRE', N'OCUPADO', N'RESERVADO')),
    -- Reglas de negocio del informe:
    CONSTRAINT UQ_Disp_Sala_Fecha_Hora   UNIQUE (id_sala,   fecha, hora_inicio),
    CONSTRAINT UQ_Disp_Medico_Fecha_Hora UNIQUE (id_medico, fecha, hora_inicio)
);
GO

-- Citas: cada bloque horario solo admite una cita (id_disponibilidad UNIQUE)
-- estado: PROGRAMADA (por defecto), EN_SALA, FINALIZADA (Strategy), CANCELADA
CREATE TABLE dbo.Cita (
    id_cita           INT IDENTITY(1,1) NOT NULL,
    id_disponibilidad INT          NOT NULL,
    id_paciente       INT          NOT NULL,
    estado            NVARCHAR(20) NOT NULL CONSTRAINT DF_Cita_estado DEFAULT N'PROGRAMADA',
    observaciones     NVARCHAR(500) NULL,
    fecha_registro    DATETIME     NOT NULL CONSTRAINT DF_Cita_fecha DEFAULT GETDATE(),
    CONSTRAINT PK_Cita PRIMARY KEY (id_cita),
    CONSTRAINT UQ_Cita_Disponibilidad UNIQUE (id_disponibilidad),
    CONSTRAINT FK_Cita_Disp     FOREIGN KEY (id_disponibilidad) REFERENCES dbo.Disponibilidad (id_disponibilidad),
    CONSTRAINT FK_Cita_Paciente FOREIGN KEY (id_paciente)       REFERENCES dbo.Paciente (id_paciente),
    CONSTRAINT CK_Cita_estado   CHECK (estado IN (N'PROGRAMADA', N'EN_SALA', N'FINALIZADA', N'CANCELADA'))
);
GO

CREATE INDEX IX_Disp_Sala_Fecha ON dbo.Disponibilidad (id_sala, fecha);
CREATE INDEX IX_Cita_Paciente   ON dbo.Cita (id_paciente);
GO

/* ---------- 3. Datos iniciales ---------- */

-- Usuarios de acceso (contraseña en texto plano, como lo valida el programa)
INSERT INTO dbo.Usuario (username, password_hash, rol, nombre_completo) VALUES
 (N'admin',      N'admin123', N'ADMIN',      N'Administrador del Sistema'),
 (N'secretario', N'secre123', N'SECRETARIO', N'María Torres (Secretaría)');

INSERT INTO dbo.Especialidad (nombre) VALUES
 (N'Medicina General'), (N'Pediatría'), (N'Cardiología'),
 (N'Ginecología'), (N'Dermatología'), (N'Traumatología'), (N'Odontología');

INSERT INTO dbo.Sala (nombre, estado) VALUES
 (N'Consultorio 1', 1), (N'Consultorio 2', 1),
 (N'Consultorio 3', 1), (N'Sala de Procedimientos', 0);

INSERT INTO dbo.Medico (codigo, nombre, especialidad, email, telefono, sexo) VALUES
 (N'DOC-001', N'Carlos Ramírez Soto',  N'Medicina General', N'cramirez@sisano.com', N'987654321', 'M'),
 (N'DOC-002', N'Lucía Fernández Paz',  N'Pediatría',        N'lfernandez@sisano.com', N'987111222', 'F'),
 (N'DOC-003', N'Jorge Medina Rojas',   N'Cardiología',      N'jmedina@sisano.com', N'987333444', 'M');

INSERT INTO dbo.Paciente (dni, nombre, email, telefono, fecha_nacimiento, genero) VALUES
 (N'45612378', N'Ana Quispe Huamán',     N'ana.quispe@mail.com',  N'955100200', '1990-05-14', 'F'),
 (N'70123456', N'Luis Gómez Salazar',    N'luis.gomez@mail.com',  N'955300400', '1985-11-02', 'M'),
 (N'41236987', N'Rosa Vargas Cárdenas',  N'rosa.vargas@mail.com', N'955500600', '1978-03-27', 'F');

-- Horarios de ejemplo: hoy y mañana (relativos a la fecha en que se ejecuta el script)
DECLARE @hoy DATE = CAST(GETDATE() AS DATE);

INSERT INTO dbo.Disponibilidad (id_medico, id_sala, fecha, hora_inicio, hora_fin, estado)
SELECT m.id_medico, s.id_sala, v.fecha, v.hi, v.hf, v.estado
FROM (VALUES
    (N'DOC-001', N'Consultorio 1', @hoy,                 CAST('08:00' AS TIME(0)), CAST('09:00' AS TIME(0)), N'ASIGNADO'),
    (N'DOC-001', N'Consultorio 1', @hoy,                 CAST('09:00' AS TIME(0)), CAST('10:00' AS TIME(0)), N'ASIGNADO'),
    (N'DOC-002', N'Consultorio 2', @hoy,                 CAST('08:00' AS TIME(0)), CAST('09:00' AS TIME(0)), N'ASIGNADO'),
    (N'DOC-003', N'Consultorio 3', @hoy,                 CAST('10:00' AS TIME(0)), CAST('11:00' AS TIME(0)), N'ASIGNADO'),
    (N'DOC-001', N'Consultorio 1', DATEADD(DAY, 1, @hoy), CAST('08:00' AS TIME(0)), CAST('09:00' AS TIME(0)), N'ASIGNADO'),
    (N'DOC-002', N'Consultorio 2', DATEADD(DAY, 1, @hoy), CAST('09:00' AS TIME(0)), CAST('10:00' AS TIME(0)), N'ASIGNADO')
) AS v (codigo, sala, fecha, hi, hf, estado)
JOIN dbo.Medico m ON m.codigo = v.codigo
JOIN dbo.Sala   s ON s.nombre = v.sala;

-- Dos citas de ejemplo (y sus bloques pasan a OCUPADO, como hace el programa)
INSERT INTO dbo.Cita (id_disponibilidad, id_paciente, observaciones)
SELECT d.id_disponibilidad, p.id_paciente, v.obs
FROM (VALUES
    (N'DOC-001', @hoy, CAST('08:00' AS TIME(0)), N'45612378', N'Control general'),
    (N'DOC-002', @hoy, CAST('08:00' AS TIME(0)), N'70123456', N'Consulta pediátrica')
) AS v (codigo, fecha, hi, dni, obs)
JOIN dbo.Medico m        ON m.codigo = v.codigo
JOIN dbo.Disponibilidad d ON d.id_medico = m.id_medico AND d.fecha = v.fecha AND d.hora_inicio = v.hi
JOIN dbo.Paciente p      ON p.dni = v.dni;

UPDATE d SET estado = N'OCUPADO'
FROM dbo.Disponibilidad d
JOIN dbo.Cita c ON c.id_disponibilidad = d.id_disponibilidad;
GO

/* ---------- 4. Verificación rápida ---------- */
SELECT 'Usuario' AS tabla, COUNT(*) AS filas FROM dbo.Usuario
UNION ALL SELECT 'Especialidad', COUNT(*) FROM dbo.Especialidad
UNION ALL SELECT 'Medico',       COUNT(*) FROM dbo.Medico
UNION ALL SELECT 'Paciente',     COUNT(*) FROM dbo.Paciente
UNION ALL SELECT 'Sala',         COUNT(*) FROM dbo.Sala
UNION ALL SELECT 'Disponibilidad', COUNT(*) FROM dbo.Disponibilidad
UNION ALL SELECT 'Cita',         COUNT(*) FROM dbo.Cita;
GO
