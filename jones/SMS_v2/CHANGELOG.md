# Changelog – AcaTrack

## [1.0.0] – Final Release

### Added
- Light academic theme with top navigation bar (AcaTrack branding)
- Dashboard with teal KPI cards (Total, Active, Inactive, Average GPA)
- Students screen with colour-coded GPA and status columns
- Search, filter, sort functionality
- Four report types: Top Performers, At-Risk, GPA Distribution, Programme Summary
- Import from CSV with validation and error logging
- Export to CSV (all students, top performers, at-risk)
- Settings: configurable at-risk GPA threshold
- Status bar at bottom of screen
- File logging to data/app.log
- 40+ unit tests across validation, reporting, and repository layers
- SQLite with prepared statements and CHECK/NOT NULL constraints
- Background threading for import/export operations
