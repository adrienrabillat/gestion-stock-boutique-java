# Gestion de stock — boutique de mode (Java)

Application de bureau pour gérer le stock et les finances d'une boutique de vêtements, chaussures et accessoires.

## Fonctionnalités

- Catalogue par catégorie (vêtements, chaussures, accessoires), tri par prix
- Ajout, modification et suppression de produits via un formulaire
- Ventes et achats de stock : chaque opération met à jour les quantités, le chiffre d'affaires, les coûts et le capital
- Remises appliquées ou retirées sur l'ensemble du catalogue

## Architecture

| Couche | Contenu |
|---|---|
| `models` | `Product` et ses sous-classes `Clothes`, `Shoes`, `Accessory` (héritage, polymorphisme) ; `Category` |
| `dao` | Interfaces DAO et implémentations JDBC (`ProductDAOImpl`, `CategoryDAOImpl`) |
| `services` | Logique métier : `ProductService` (catalogue, ventes, remises) et `FinancialService` (capital, recettes, coûts) |
| `controllers` + `views` | Interface JavaFX en FXML (`MainView`, `ProductForm`) |
| `utils` | Connexion MySQL en singleton, validation des saisies |

## Stack

Java, JavaFX (FXML), MySQL avec JDBC, Maven, JUnit 5.

## Lancer le projet

```bash
export DB_URL=jdbc:mysql://localhost:3306/womenshop_db
export DB_USER=root
export DB_PASSWORD=...
./mvnw javafx:run
```
