#!/bin/bash
# DEPRECATED: Schema creation workflow is now handled universally by seed_data.sh
# which sequences schemas followed directly by seed injects.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Notice: create_schema.sh is deprecated. Executing seed_data.sh instead."
"$SCRIPT_DIR/seed_data.sh"